package com.perozzo.cardapiosapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kumulos.android.Kumulos;
import com.kumulos.android.ResponseHandler;
import com.perozzo.cardapiosapp.R;
import com.perozzo.cardapiosapp.classes.Restaurant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ManageRestaurantsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public RecyclerView restaurants_rv;
    public SwipeRefreshLayout refresh_srl;
    public TextView header_tv;
    public FloatingActionButton fab;

    public Context ctx;
    FirebaseAuth mAuth;
    public String userID;

    public List<Restaurant> restaurantsList;
    public RestaurantsAdapter restaurantsAdapter;
    private SharedPreferences sharedPrefSettings;

    public int getRestaurantsError = 0;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_restaurants);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setBackgroundDrawable(null);
        mAuth = FirebaseAuth.getInstance();
        ctx = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    }
                });
                builder.show();
            }
        }

        sharedPrefSettings = getSharedPreferences("CARDAPIOSAPP", 0);

        refresh_srl = (SwipeRefreshLayout) findViewById(R.id.refresh_srl);
        refresh_srl.setColorSchemeResources(R.color.bordoMain);
        refresh_srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRestaurants();
            }
        });
        header_tv = (TextView) findViewById(R.id.header_msg);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ManageRestaurantsActivity.this, AddEditRestaurantsActivity.class);
                startActivity(i);
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        intent = getIntent();

        if(mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
        restaurantsList = new ArrayList<>();
        restaurants_rv = (RecyclerView) findViewById(R.id.restaurants_rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        restaurants_rv.setLayoutManager(llm);

        if(isOnline()) {
            getRestaurants();
        }
        else{
            Toast.makeText(getApplicationContext(), getString(R.string.noconnection), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onRestart() {

        super.onRestart();
    }

    public void clear() {
        int size = this.restaurantsList.size();
        if(size > 0){
            this.restaurantsList.clear();
            restaurantsAdapter.notifyItemRangeRemoved(0, size);
        }
    }

    public void getRestaurants(){
        clear();

        header_tv.setVisibility(View.GONE);
        refresh_srl.setRefreshing(true);
        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("owner", userID); //owner
        Kumulos.call("getRestaurants",params1, new ResponseHandler() {
            @Override
            public void onFailure(@Nullable Throwable error) {
                getRestaurantsError++;
                if(getRestaurantsError>= 3){
                    getRestaurantsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    fab.setVisibility(View.VISIBLE);
                    return;
                }
                else{
                    getRestaurants();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;
                if(!objects.isEmpty()){
                    header_tv.setVisibility(View.GONE);
                }
                else{
                    header_tv.setVisibility(View.VISIBLE);
                }

                for(int i = 0; i < objects.size(); i++){
                    Restaurant r = new Restaurant();
                    Gson gson = new Gson();
                    String json = gson.toJson(objects.get(i));
                    r = gson.fromJson(json, Restaurant.class);
                    restaurantsList.add(r);
                }
                restaurantsAdapter = new RestaurantsAdapter(ctx,restaurantsList);
                restaurants_rv.setAdapter(restaurantsAdapter);
                fab.setVisibility(View.VISIBLE);
                refresh_srl.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        restaurantsAdapter.clear();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_scan) {
            invalidateOptionsMenu();
            Intent i = new Intent(ManageRestaurantsActivity.this, Main2Activity.class);
            startActivity(i);
            finish();
        } else if (id == R.id.nav_logout) {
            invalidateOptionsMenu();
            LogOut();
            Intent i = new Intent(ManageRestaurantsActivity.this, Main2Activity.class);
            startActivity(i);
            finish();
        } else if(id == R.id.nav_change_account){
            //TODO
            invalidateOptionsMenu();
            Intent i = new Intent(ManageRestaurantsActivity.this, UserSettings.class);
            startActivity(i);
        }
        else if(id == R.id.nav_delete){

            ShowDeleteConfirmDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    AlertDialog dialog;
    public void ShowDeleteConfirmDialog(){
        //TODO
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage("Tem certeza que deseja excluir o usuário?")
                .setTitle("Deletar Usuário");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                ProgressDialog();
                deleteUser();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();

    }

    public ProgressDialog progressDialog;
    public void ProgressDialog(){
        progressDialog = new ProgressDialog(ctx);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.empty_progress_dialog);
        progressDialog.setIndeterminate(true);

    }

    public void deleteUser(){
        FirebaseUser user = mAuth.getCurrentUser();
        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(ctx, getString(R.string.removed_user), Toast.LENGTH_LONG).show();
                LogOut();
                deleteRestaurantsByUser();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ctx, getString(R.string.removed_user_fail), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deleteRestaurantsByUser(){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("owner", userID);
        Kumulos.call("removeRestaurantsByUser", params, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                progressDialog.dismiss();
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                if ((int) result >= 1) {
                    Toast.makeText(ctx, "Restaurantes do usuário excluídos!", Toast.LENGTH_SHORT).show();
                }

                progressDialog.dismiss();
                LogOut();
                Intent i = new Intent(ManageRestaurantsActivity.this, Main2Activity.class);
                startActivity(i);
                finish();
                super.didCompleteWithResult(result);
            }
        });
    }




    private void setSetting(String tag, String value) {
        try {
            SharedPreferences.Editor editor = sharedPrefSettings.edit();
            editor.putString(tag, value);
            editor.commit();
        } catch (Exception e) {
            Log.v("CardapiosApp", "err:"+e.getMessage());
        }
    }

    public void SavePictureData(String pictureData){
        setSetting("PICTUREDATA", pictureData);
    }

    public void LogOut(){
        FirebaseAuth.getInstance().signOut();
        setSetting("EMAIL", "");
        setSetting("PASSWORD", "");
    }

    public class RestaurantsAdapter extends RecyclerView.Adapter<RestaurantsAdapter.RViewHolder>{
        private List<Restaurant> mList;
        private LayoutInflater mLayoutInflater;
        private Context ctx;

        public RestaurantsAdapter(Context c, List<Restaurant> l){
            ctx = c;
            mList = l;
            mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void clear() {
            int size = this.mList.size();
            this.mList.clear();
            this.notifyDataSetChanged();
        }

        @Override
        public RViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(R.layout.restaurant_item, parent, false);
            RViewHolder mvh =  new RViewHolder(view);
            return mvh;
        }

        @Override
        public void onBindViewHolder(RViewHolder holder, int position) {
            holder.name_tv.setText(mList.get(position).name);
            holder.address_tv.setText(mList.get(position).fullAddr);
            byte[] decodedString = Base64.decode(mList.get(position).icon, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.image_img.setImageBitmap(decodedByte);
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class RViewHolder extends RecyclerView.ViewHolder{
            public TextView name_tv;
            public TextView address_tv;
            public ImageView image_img;
            public TextView edit_btn;
            public TextView manageCards_btn;

            public RViewHolder(View itemView) {
                super(itemView);

                name_tv = (TextView) itemView.findViewById(R.id.name_tv);
                address_tv = (TextView) itemView.findViewById(R.id.address_tv);
                image_img = (ImageView) itemView.findViewById(R.id.image_img);
                edit_btn = (TextView) itemView.findViewById(R.id.edit_btn);
                manageCards_btn = (TextView) itemView.findViewById(R.id.manageCards_btn);

                edit_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(ManageRestaurantsActivity.this, AddEditRestaurantsActivity.class);
                        i.putExtra("Restaurant",restaurantsList.get(getAdapterPosition()));
                        SavePictureData(restaurantsList.get(getAdapterPosition()).icon);
                        restaurantsList.get(getAdapterPosition()).icon = "";
                        startActivity(i);
                    }
                });

                manageCards_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(ManageRestaurantsActivity.this, ManageCardsActivity.class);
                        i.putExtra("restaurantID",restaurantsList.get(getAdapterPosition()).restaurantID);
                        i.putExtra("LATITUDE", restaurantsList.get(getAdapterPosition()).latitude);
                        i.putExtra("LONGITUDE", restaurantsList.get(getAdapterPosition()).longitude);
                        i.putExtra("NAME", restaurantsList.get(getAdapterPosition()).name);
                        i.putExtra("CITY", restaurantsList.get(getAdapterPosition()).city);
                        startActivity(i);
                    }
                });


            }
        }
    }
}
