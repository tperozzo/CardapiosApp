package com.perozzo.cardapiosapp.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.kumulos.android.Kumulos;
import com.kumulos.android.ResponseHandler;
import com.perozzo.cardapiosapp.R;
import com.perozzo.cardapiosapp.classes.Cardy;
import com.perozzo.cardapiosapp.components.SimpleDividerItemDecorationCardies;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

//TODO lista de cardapios de um restaurante
// cada item da lista tem que ter botão EXCLUIR e EDITAR,
// se clicou em EXCLUIR mostrar janela de confirmação
// se clicou EDITAR vai pra tela do cardápio mandando as infos na intent
// floating button ADD
// se clicou ADD vai pra tela de cardápio sem mandar nada

public class ManageCardsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public List<Cardy> cardsList;
    public RecyclerView cards_rv;
    public CardsAdapter cardsAdapter;

    public SwipeRefreshLayout refresh_srl;
    public FloatingActionButton fab;

    public String userID;
    public String restaurantID;
    public String name;
    public String city;
    public double latitude, longitude;
    public Context ctx;

    private SharedPreferences sharedPrefSettings;

    final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    final SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy");

    public int getCardsError = 0;
    public int removeCardByDateError = 0;
    public TextView header_tv;
    public Cardy c;

    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_cardies);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setBackgroundDrawable(null);
        mAuth = FirebaseAuth.getInstance();

        refresh_srl = (SwipeRefreshLayout) findViewById(R.id.refresh_srl);
        refresh_srl.setColorSchemeResources(R.color.bordoMain);
        header_tv = (TextView) findViewById(R.id.header_msg);

        ctx = this;
        sharedPrefSettings = getSharedPreferences("CARDAPIOSAPP", 0);

        final Intent intent = getIntent();
        restaurantID = intent.getStringExtra("restaurantID");
        userID = mAuth.getCurrentUser().getUid();
        latitude = intent.getDoubleExtra("LATITUDE", 0);
        longitude = intent.getDoubleExtra("LONGITUDE", 0);
        name = intent.getStringExtra("NAME");
        city = intent.getStringExtra("CITY");

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ManageCardsActivity.this, AddEditCardActivity.class);
                i.putExtra("restaurantID",restaurantID);
                i.putExtra("LATITUDE",latitude);
                i.putExtra("LONGITUDE",longitude);
                i.putExtra("NAME", name);
                i.putExtra("CITY", city);
                i.putExtra("isADD", true);
                startActivity(i);
            }
        });

        refresh_srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCards();
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

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onResume() {
        super.onResume();

        cardsList = new ArrayList<>();
        cards_rv = (RecyclerView) findViewById(R.id.cards_rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        cards_rv.setLayoutManager(llm);

        if(isOnline()) {
            getCards();
        }
        else{
            Toast.makeText(getApplicationContext(), getString(R.string.noconnection), Toast.LENGTH_SHORT).show();
        }
    }

    public void clear() {
        int size = this.cardsList.size();
        if(size > 0){
            this.cardsList.clear();
            cardsAdapter.notifyItemRangeRemoved(0, size);
        }
    }

    public void getCards(){
        clear();
        header_tv.setVisibility(View.GONE);
        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("restaurant", restaurantID);
        refresh_srl.setRefreshing(true);
        Kumulos.call("getCards",params1, new ResponseHandler() {
            @Override
            public void onFailure(@Nullable Throwable error) {
                getCardsError++;
                if(getCardsError>= 3){
                    getCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                else{
                    getCards();
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
                    c = new Cardy();
                    c.cardID = String.valueOf(objects.get(i).get("cardID"));
                    if(objects.get(i).get("daysOfWeek").equals("")) {
                        String dateString = String.valueOf(objects.get(i).get("date")).substring(0, 10);
                        try {
                            c.date = new Date(format.parse(dateString).getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Calendar cal = Calendar.getInstance();
                        String day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
                        String month = String.format("%02d", cal.get(Calendar.MONTH) + 1);
                        String year = String.format("%04d", cal.get(Calendar.YEAR));
                        String nowString = day + "/" + month + "/" + year;
                        Date now = null;
                        try {
                            now = new Date(format2.parse(nowString).getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (c.date.getTime() < now.getTime()) {
                            removeCardByDate();
                        } else {
                            c.card = String.valueOf(objects.get(i).get("card"));
                            cardsList.add(c);
                        }
                    }
                    else{
                        //TODO card daysofweek
                        String dateString = "1970-01-01";
                        try {
                            c.date = new Date(format.parse(dateString).getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        c.daysOfWeek = String.valueOf(objects.get(i).get("daysOfWeek"));
                        c.card = String.valueOf(objects.get(i).get("card"));
                        cardsList.add(c);
                    }

                    Collections.sort(cardsList, new Comparator<Cardy>() {
                        @Override public int compare(Cardy bo1, Cardy bo2) {
                            return (bo1.date.getTime() >  bo2.date.getTime() ? 1:-1);
                        }
                    });
                }

                cardsAdapter = new CardsAdapter(ctx, cardsList);
                cards_rv.addItemDecoration(new SimpleDividerItemDecorationCardies(ctx));
                cards_rv.setAdapter(cardsAdapter);
                fab.setVisibility(View.VISIBLE);
                refresh_srl.setRefreshing(false);
            }
        });
    }

    public void removeCardByDate(){
        HashMap<String, String> params2 = new HashMap<String, String>();
        params2.put("restaurant", restaurantID);
        params2.put("cardID", c.cardID);
        Kumulos.call("removeCardByDate", params2, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                removeCardByDateError++;
                if(removeCardByDateError>= 3){
                    removeCardByDateError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    removeCardByDate();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
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

    public void LogOut(){
        setSetting("EMAIL", "");
        setSetting("PASSWORD", "");
    }

    @Override
    protected void onStop() {
        super.onStop();
        clear();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        finish();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_scan) {
            Intent i = new Intent(ManageCardsActivity.this, Main2Activity.class);
            startActivity(i);
            finish();
        } else if (id == R.id.nav_logout) {
            LogOut();
            Intent i = new Intent(ManageCardsActivity.this, Main2Activity.class);
            startActivity(i);
            finish();
        } else if(id == R.id.nav_manage_rests){
            Intent i = new Intent(ManageCardsActivity.this, ManageRestaurantsActivity.class);
            startActivity(i);
            finish();
        }else if(id == R.id.nav_change_account){
            Intent i = new Intent(ManageCardsActivity.this, UserSettings.class);
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
                .setTitle("Deletar Cardápio");

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
                if((int)result >= 1) {
                    Toast.makeText(ctx, "Restaurantes do usuário excluídos!", Toast.LENGTH_SHORT).show();
                }

                LogOut();
                progressDialog.dismiss();
                Intent i = new Intent(ManageCardsActivity.this, Main2Activity.class);
                i.putExtra("logged", false);
                startActivity(i);
                finish();
                super.didCompleteWithResult(result);
            }


        });
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

    //TODO
    public void deleteUser(){
        HashMap<String, String> params = new HashMap<String, String>();
        //params.put("email", login);
        //params.put("password", password);
        Kumulos.call("removeUser", params, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                progressDialog.dismiss();
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                if((int)result == 1) {
                    Toast.makeText(ctx, "Usuário excluído!", Toast.LENGTH_SHORT).show();
                    LogOut();
                    deleteRestaurantsByUser();
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(ctx, "Não foi possível excluir o usuário.", Toast.LENGTH_SHORT).show();
                }
                super.didCompleteWithResult(result);
            }
        });
    }

    public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CViewHolder>{
        private List<Cardy> mList;
        private LayoutInflater mLayoutInflater;
        private Context ctx;

        public CardsAdapter(Context c, List<Cardy> l){
            ctx = c;
            mList = l;
            mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public CViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(R.layout.card_item, parent, false);
            CViewHolder mvh =  new CViewHolder(view);
            return mvh;
        }

        @Override
        public void onBindViewHolder(CViewHolder holder, int position) {
            String listString;
            if(mList.get(position).daysOfWeek.equals("")) {
                listString = format2.format(mList.get(position).date);
            }
            else{
                listString = mList.get(position).daysOfWeek;
                listString = listString.replace("sun", getString(R.string.sunday) + ", ");
                listString = listString.replace("mon", getString(R.string.monday) + ", ");
                listString = listString.replace("tue", getString(R.string.tuesday) + ", ");
                listString = listString.replace("wed", getString(R.string.wednesday) + ", ");
                listString = listString.replace("thu", getString(R.string.thursday) + ", ");
                listString = listString.replace("fri", getString(R.string.friday) + ", ");
                listString = listString.replace("sat", getString(R.string.saturday) + ", ");

                listString = listString.substring(0, listString.length() - 2);
            }
            holder.date_tv.setText(listString);
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

        public class CViewHolder extends RecyclerView.ViewHolder{
            public TextView date_tv;
            public LinearLayout ll;

            public CViewHolder(View itemView) {
                super(itemView);
                ll = (LinearLayout) itemView.findViewById(R.id.card_ll);
                date_tv = (TextView) itemView.findViewById(R.id.date_tv);

                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(ManageCardsActivity.this, AddEditCardActivity.class);
                        i.putExtra("restaurantID",restaurantID);
                        i.putExtra("CARD", cardsList.get(getAdapterPosition()));
                        i.putExtra("isADD", false);
                        i.putExtra("LATITUDE",latitude);
                        i.putExtra("LONGITUDE",longitude);
                        i.putExtra("NAME", name);
                        i.putExtra("CITY", city);
                        startActivity(i);
                    }
                });

                date_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(ManageCardsActivity.this, AddEditCardActivity.class);
                        i.putExtra("restaurantID",restaurantID);
                        i.putExtra("CARD", cardsList.get(getAdapterPosition()));
                        i.putExtra("isADD", false);
                        i.putExtra("LATITUDE",latitude);
                        i.putExtra("LONGITUDE",longitude);
                        i.putExtra("NAME", name);
                        i.putExtra("CITY", city);
                        startActivity(i);
                    }
                });

            }
        }
    }
}
