package com.perozzo.cardapiosapp.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.kumulos.android.Kumulos;
import com.kumulos.android.ResponseHandler;
import com.perozzo.cardapiosapp.R;
import com.perozzo.cardapiosapp.classes.Cardy;
import com.perozzo.cardapiosapp.classes.Restaurant;
import com.perozzo.cardapiosapp.classes.Searcheds;
import com.perozzo.cardapiosapp.components.SimpleDividerItemDecoration;

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

//Push change

public class Main2Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public boolean name_en, food_en, city_en, dist_en;
    public String name_str, food_str, city_str, dist_str;
    public String dayOfWeek;

    public boolean isLogged = false;
    public Context ctx;
    public String userID;
    private SharedPreferences sharedPrefSettings;

    public GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    public double latitude, longitude;
    public final double K = 0.01070928; //1km em graus
    public double latmin, latmax, longmin, longmax;
    public boolean locationPermission = false;
    public boolean firstUse;

    public int loginError = 0;
    public int searchCardsError = 0;

    public InterstitialAd mInterstitialAd;
    public int adViewCount;
    public Intent i;
    Toolbar toolbar;

    public FirebaseAuth mAuth;

    public RecyclerView results_rv;
    public LinearLayout logoLayout;
    public ArrayList<Searcheds> resultsList;
    public ResultsAdapter resultsAdapter;
    public TextView msg_tv;
    public SwipeRefreshLayout refresh_srl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setBackgroundDrawable(null);
        mAuth = FirebaseAuth.getInstance();
        LocationPermission();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        sharedPrefSettings = getSharedPreferences("CARDAPIOSAPP", 0);
        firstUse = sharedPrefSettings.getBoolean("firstUse", true);

        ctx = this;

        if(mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
            isLogged = true;
        }
        else{
            userID = "";
            isLogged = false;
        }
        MakeDrawer();


        name_en = getSetting("name_en", false);
        food_en = getSetting("food_en", false);
        city_en = getSetting("city_en", false);
        dist_en = getSetting("dist_en", true);
        name_str = getSetting("name_str", "");
        food_str = getSetting("food_str", "");
        city_str = getSetting("city_str", "");
        dist_str = getSetting("dist_str", "10");

        if(firstUse){
            dist_en = true;
            dist_str = "10";
        }

        //LocationPermission();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.SUNDAY:
                dayOfWeek = "sun";
                break;

            case Calendar.MONDAY:
                dayOfWeek = "mon";
                break;

            case Calendar.TUESDAY:
                dayOfWeek = "tue";
                break;

            case Calendar.WEDNESDAY:
                dayOfWeek = "wed";
                break;

            case Calendar.THURSDAY:
                dayOfWeek = "thu";
                break;

            case Calendar.FRIDAY:
                dayOfWeek = "fri";
                break;

            case Calendar.SATURDAY:
                dayOfWeek = "sat";
                break;
        }

        adViewCount = Integer.parseInt(getSetting("ADVIEWCOUNT", "0"));
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                startActivity(i);
            }
        });

        requestNewInterstitial();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        adViewCount = Integer.parseInt(getSetting("ADVIEWCOUNT", "0"));
        mInterstitialAd = new InterstitialAd(ctx);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                startActivity(i);
            }
        });

        requestNewInterstitial();
        resultsList = new ArrayList<>();

        refresh_srl = (SwipeRefreshLayout) findViewById(R.id.refresh_srl);
        refresh_srl.setRefreshing(false);
        refresh_srl.setColorSchemeResources(R.color.bordoMain);
        refresh_srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                search();
            }
        });

        msg_tv = (TextView) findViewById(R.id.msg_tv);
        logoLayout = (LinearLayout) findViewById(R.id.logo_layout);

        results_rv = (RecyclerView) findViewById(R.id.restaurants_rv);

        LinearLayoutManager llm = new LinearLayoutManager(results_rv.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        results_rv.setLayoutManager(llm);

        if(!dist_en)
            search();
    }

    public void search(){
        logoLayout.setVisibility(View.GONE);

        if(isOnline()) {
            //N
            if((name_en)&&(!food_en)&&(!city_en)&&(!dist_en)) {
                searchCardsN();
            }

            //F
            else if((!name_en)&&(food_en)&&(!city_en)&&(!dist_en)) {
                searchCardsF();
            }

            //C
            else if((!name_en)&&(!food_en)&&(city_en)&&(!dist_en)) {
                searchCardsC();
            }

            //D
            else if((!name_en)&&(!food_en)&&(!city_en)&&(dist_en)) {
                if (checkGPSEnabled() && locationPermission)
                    searchCardsD();
                else if (!locationPermission) {
                    LocationPermission();
                }
            }

            //NF
            else if((name_en)&&(food_en)&&(!city_en)&&(!dist_en)) {
                searchCardsNF();
            }

            //NC
            else if((name_en)&&(!food_en)&&(city_en)&&(!dist_en)) {
                searchCardsNC();
            }

            //ND
            else if((name_en)&&(!food_en)&&(!city_en)&&(dist_en)) {
                if (checkGPSEnabled() && locationPermission)
                    searchCardsND();
                else if (!locationPermission) {
                    LocationPermission();
                }
            }

            //FC
            else if((!name_en)&&(food_en)&&(city_en)&&(!dist_en)) {
                searchCardsFC();
            }

            //CD
            else if((name_en)&&(!food_en)&&(city_en)&&(!dist_en)) {
                if (checkGPSEnabled() && locationPermission)
                    searchCardsCD();
                else if (!locationPermission) {
                    LocationPermission();
                }
            }

            //FD
            else if((!name_en)&&(food_en)&&(!city_en)&&(dist_en)) {
                if (checkGPSEnabled() && locationPermission)
                    searchCardsFD();
                else if (!locationPermission) {
                    LocationPermission();
                }
            }

            //CD
            else if((!name_en)&&(!food_en)&&(city_en)&&(dist_en)) {
                if (checkGPSEnabled() && locationPermission)
                    searchCardsCD();
                else if (!locationPermission) {
                    LocationPermission();
                }
            }

            //NFC
            else if((name_en)&&(food_en)&&(city_en)&&(!dist_en)) {
                searchCardsNFC();
            }

            //NFD
            else if((name_en)&&(food_en)&&(!city_en)&&(dist_en)) {
                if (checkGPSEnabled() && locationPermission)
                    searchCardsNFD();
                else if (!locationPermission) {
                    LocationPermission();
                }
            }

            //NCD
            else if((name_en)&&(!food_en)&&(city_en)&&(dist_en)) {
                if (checkGPSEnabled() && locationPermission)
                    searchCardsNCD();
                else if (!locationPermission) {
                    LocationPermission();
                }
            }

            //FCD
            else if((!name_en)&&(food_en)&&(city_en)&&(dist_en)) {
                if (checkGPSEnabled() && locationPermission)
                    searchCardsFCD();
                else if (!locationPermission) {
                    LocationPermission();
                }
            }

            //NFCD
            else if((name_en)&&(food_en)&&(city_en)&&(dist_en)) {
                if (checkGPSEnabled() && locationPermission)
                    searchCardsNFCD();
                else if (!locationPermission) {
                    LocationPermission();
                }
            }

            else{
                refresh_srl.setRefreshing(false);
                Toast.makeText(ctx, R.string.search_msg, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(ctx, getString(R.string.noconnection), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public class Adapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }



        @Override
        public Fragment getItem(int position) {
            invalidateOptionsMenu();
            Bundle args = new Bundle();
            //args.putParcelable(DataDevice.TAG, tagFromIntent);
            //mBluetoothLeService.ParseRHRData();
            mFragments.get(position).setArguments(args);
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            ShowSearchDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void ShowSearchDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Busca personalizada");
        dialog.setContentView(R.layout.search_dialog);

        final CheckBox name_cb = (CheckBox) dialog.findViewById(R.id.name_cb);
        final CheckBox food_cb = (CheckBox) dialog.findViewById(R.id.food_cb);
        final CheckBox city_cb = (CheckBox) dialog.findViewById(R.id.city_cb);
        final CheckBox dist_cb = (CheckBox) dialog.findViewById(R.id.dist_cb);
        final EditText name_et = (EditText) dialog.findViewById(R.id.name_et);
        final EditText food_et = (EditText) dialog.findViewById(R.id.food_et);
        final EditText city_et = (EditText) dialog.findViewById(R.id.city_et);
        final TextView dist_tv = (TextView) dialog.findViewById(R.id.dist_tv);
        final SeekBar dist_sb = (SeekBar) dialog.findViewById(R.id.dist_sb);
        final TextView cancel_tv = (TextView) dialog.findViewById(R.id.cancel_tv);
        final TextView ok_tv = (TextView) dialog.findViewById(R.id.ok_tv);

        name_cb.setChecked(name_en);
        name_et.setEnabled(name_en);
        name_et.setText(name_str);

        food_cb.setChecked(food_en);
        food_et.setEnabled(food_en);
        food_et.setText(food_str);

        city_cb.setChecked(city_en);
        city_et.setEnabled(city_en);
        city_et.setText(city_str);

        dist_cb.setChecked(dist_en);
        dist_sb.setEnabled(dist_en);
        dist_sb.setProgress(Integer.parseInt(dist_str));
        dist_tv.setText(" " + dist_str + " km");

        name_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(name_cb.isChecked()) {
                    name_et.setEnabled(true);
                    name_et.setText(name_str);
                }
                else {
                    if(name_et.getText().toString().length() > 3)
                        name_str = name_et.getText().toString();
                    name_et.setText(getString(R.string.name_hint));
                    name_et.setEnabled(false);
                }
            }
        });

        food_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(food_cb.isChecked()) {
                    food_et.setEnabled(true);
                    food_et.setText(food_str);
                }
                else {
                    if(food_et.getText().toString().length() > 3)
                        food_str = food_et.getText().toString();
                    food_et.setText(getString(R.string.food));
                    food_et.setEnabled(false);
                }
            }
        });

        city_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(city_cb.isChecked()) {
                    city_et.setEnabled(true);
                    city_et.setText(city_str);
                }
                else {
                    if(city_et.getText().toString().length() > 3)
                        city_str = city_et.getText().toString();
                    city_et.setText(getString(R.string.city_hint));
                    city_et.setEnabled(false);
                }
            }
        });

        dist_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(dist_cb.isChecked())
                    dist_sb.setEnabled(true);
                else
                    dist_sb.setEnabled(false);
            }
        });

        dist_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(dist_sb.getProgress() < 1)
                    dist_sb.setProgress(1);
                dist_tv.setText(" " + String.valueOf(dist_sb.getProgress())+ " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        cancel_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ok_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name_en = name_cb.isChecked();
                food_en = food_cb.isChecked();
                city_en = city_cb.isChecked();
                dist_en = dist_cb.isChecked();
                name_str = name_et.getText().toString();
                food_str = food_et.getText().toString();
                city_str = city_et.getText().toString();
                dist_str = String.valueOf(dist_sb.getProgress());

                if((name_en)&&(name_et.getText().toString().length()<3))
                    Toast.makeText(ctx, R.string.rest_name_msg, Toast.LENGTH_LONG).show();
                else if((food_en)&&(food_et.getText().toString().length()<3))
                    Toast.makeText(ctx, R.string.food_msg, Toast.LENGTH_LONG).show();
                else if((city_en)&&(city_et.getText().toString().length()<3))
                    Toast.makeText(ctx, R.string.city_msg, Toast.LENGTH_LONG).show();
                else if((!name_en)&&(!food_en)&&(!city_en)&&(!dist_en))
                    Toast.makeText(ctx, R.string.search_msg, Toast.LENGTH_LONG).show();
                else {
                    setSetting("name_en", name_en);
                    setSetting("food_en", food_en);
                    setSetting("city_en", city_en);
                    setSetting("dist_en", dist_en);
                    setSetting("name_str", name_str);
                    setSetting("food_str", food_str);
                    setSetting("city_str", city_str);
                    setSetting("dist_str", dist_str);

                    search();
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    public void MakeDrawer() {

        invalidateOptionsMenu();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        navigationView.getMenu().clear();
        if (isLogged)
            navigationView.inflateMenu(R.menu.scan_logged);
        else
            navigationView.inflateMenu(R.menu.scan_notlogged);
    }


    public void LocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.need_location_title));
                builder.setMessage(getString(R.string.need_location_msg));
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    }
                });
                builder.show();
            }
            else{
                locationPermission = true;
            }
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("89CC9100943D6BF03D38BBF9775C79FA")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private String getSetting(String tag, String defaultReturn) {
        try {
            return sharedPrefSettings.getString(tag, defaultReturn);
        } catch (Exception e) {
            return defaultReturn;
        }
    }

    private boolean getSetting(String tag, boolean defaultReturn) {
        try {
            return sharedPrefSettings.getBoolean(tag, defaultReturn);
        } catch (Exception e) {
            return defaultReturn;
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        if(firstUse)
            sharedPrefSettings.edit().putBoolean("firstUse", false).commit();
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            // start login Act
            invalidateOptionsMenu();
            Intent i = new Intent(Main2Activity.this, UserLoginActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_signup) {
            //start signUp Act
            invalidateOptionsMenu();
            Intent i = new Intent(Main2Activity.this, UserSignUpActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_manage_rests) {
            Intent i = new Intent(Main2Activity.this, ManageRestaurantsActivity.class);
            startActivity(i);
            finish();
        } else if (id == R.id.nav_logout) {
            LogOut();
            MakeDrawer();
        }else if(id == R.id.nav_change_account){
            Intent i = new Intent(Main2Activity.this, UserSettings.class);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage("Tem certeza que deseja excluir o usuário?")
                .setTitle("Deletar Usuário");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                if(isOnline()) {
                    ProgressDialog();
                    deleteUser();
                }
                else {
                    Toast.makeText(getApplicationContext(), getString(R.string.noconnection), Toast.LENGTH_SHORT).show();
                }
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

    //TODO
    public void deleteUser(){
        HashMap<String, String> params = new HashMap<String, String>();
        //params.put("email", email);
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
                    progressDialog.dismiss();
                    Toast.makeText(ctx, "Restaurantes do usuário excluídos!", Toast.LENGTH_SHORT).show();
                }
                else{
                    progressDialog.dismiss();
                }
                LogOut();
                super.didCompleteWithResult(result);
            }


        });
    }

    public void LogOut() {
        FirebaseAuth.getInstance().signOut();
        isLogged = false;
        setSetting("EMAIL", "");
        setSetting("PASSWORD", "");
    }

    private void setSetting(String tag, String value) {
        try {
            SharedPreferences.Editor editor = sharedPrefSettings.edit();
            editor.putString(tag, value);
            editor.commit();
        } catch (Exception e) {
            Log.v("CardapiosApp", "err:" + e.getMessage());
        }
    }

    private void setSetting(String tag, boolean value) {
        try {
            SharedPreferences.Editor editor = sharedPrefSettings.edit();
            editor.putBoolean(tag, value);
            editor.commit();
        } catch (Exception e) {
            Log.v("CardapiosApp", "err:" + e.getMessage());
        }
    }

    public void CalcParameters(){
        latmin = latitude - (K * Integer.valueOf(dist_str));
        latmax = latitude + (K * Integer.valueOf(dist_str));
        longmin = longitude - (K * Math.cos(latitude) * Integer.valueOf(dist_str));
        longmax = longitude + (K * Math.cos(latitude) * Integer.valueOf(dist_str));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            CalcParameters();
            if((dist_en)&&(!firstUse))
                search();
        }
        else{
            //TODO TOAST msg_tv.setText("Localização não encontrada. \nVerifique sua conexão e suas permissões.");
        }
    }

    public Date getNow(){
        Calendar cal = Calendar.getInstance();
        String day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
        String month = String.format("%02d", cal.get(Calendar.MONTH) + 1);
        String year = String.format("%04d", cal.get(Calendar.YEAR));
        String nowString = day + "/" + month + "/" + year;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Date now = null;
        try {
            now = new java.util.Date(format.parse(nowString).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return now;
    }

    public boolean checkGPSEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return true;
        }else{
            showGPSDisabledAlertToUser();
            return false;
        }
    }

    public void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.GPS_msg))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.GPS_btn),
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    public double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }

    public double CalcDistance(double latRest, double longRest)
    {
        int R = 6371;
        double dLat = deg2rad(latitude - latRest);
        double dLong = deg2rad(longitude - longRest);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(deg2rad(latitude)) * Math.cos(deg2rad(latRest)) *
                        Math.sin(dLong/2) * Math.sin(dLong/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;

        return d;
    }

    public void searchCardsN(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        HashMap<String, String> params1 = new HashMap<String, String>();

        params1.put("daysOfWeek", dayOfWeek);
        params1.put("restaurantName", name_str);

        Kumulos.call("searchCardsNRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsN();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsN();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    Gson gson = new Gson();
                                    String json = gson.toJson(restaurantMap);
                                    s.r = gson.fromJson(json, Restaurant.class);
                                    /*
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));
                                    */
                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsN();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsF(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        HashMap<String, String> params1 = new HashMap<String, String>();

        params1.put("daysOfWeek", dayOfWeek);
        params1.put("food", food_str);

        Kumulos.call("searchCardsFRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsF();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("food", food_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsCSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsF();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("food", food_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsFSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsF();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsC(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        HashMap<String, String> params1 = new HashMap<String, String>();

        params1.put("daysOfWeek", dayOfWeek);
        params1.put("city", city_str);

        Kumulos.call("searchCardsCRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsC();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("city", city_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsCSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsC();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("city", city_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsCSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsC();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsD(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        HashMap<String, String> params1 = new HashMap<String, String>();

        CalcParameters();

        params1.put("daysOfWeek", dayOfWeek);
        params1.put("latmin", String.valueOf(latmin));
        params1.put("latmax", String.valueOf(latmax));
        params1.put("longmin", String.valueOf(longmin));
        params1.put("longmax", String.valueOf(longmax));

        Kumulos.call("searchCardsDRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsD();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsNF(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        HashMap<String, String> params1 = new HashMap<String, String>();

        params1.put("daysOfWeek", dayOfWeek);
        params1.put("restaurantName", name_str);
        params1.put("food", food_str);

        Kumulos.call("searchCardsNFRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsNF();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("food", food_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNFSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsNF();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("food", food_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNFSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsNF();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsNC(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        HashMap<String, String> params1 = new HashMap<String, String>();

        params1.put("daysOfWeek", dayOfWeek);
        params1.put("restaurantName", name_str);
        params1.put("city", city_str);

        Kumulos.call("searchCardsNCRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsNC();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("city", city_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNCSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsNC();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("city", city_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNCSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsNC();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsND(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        CalcParameters();

        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("daysOfWeek", dayOfWeek);
        params1.put("restaurantName", name_str);
        params1.put("latmin", String.valueOf(latmin));
        params1.put("latmax", String.valueOf(latmax));
        params1.put("longmin", String.valueOf(longmin));
        params1.put("longmax", String.valueOf(longmax));

        Kumulos.call("searchCardsNDRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsND();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsND();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsND();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsFC(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        HashMap<String, String> params1 = new HashMap<String, String>();

        params1.put("daysOfWeek", dayOfWeek);
        params1.put("food", food_str);
        params1.put("city", city_str);

        Kumulos.call("searchCardsFCRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsFC();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("food", food_str);
                    params2.put("city", city_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsFCSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsFC();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("food", food_str);
                    params2.put("city", city_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsFCSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsFC();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsFD(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        CalcParameters();

        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("daysOfWeek", dayOfWeek);
        params1.put("food", food_str);
        params1.put("latmin", String.valueOf(latmin));
        params1.put("latmax", String.valueOf(latmax));
        params1.put("longmin", String.valueOf(longmin));
        params1.put("longmax", String.valueOf(longmax));

        Kumulos.call("searchCardsFDRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsFD();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("food", food_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsFDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsFD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("food", food_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsFDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsFD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsCD(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        CalcParameters();

        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("daysOfWeek", dayOfWeek);
        params1.put("city", city_str);
        params1.put("latmin", String.valueOf(latmin));
        params1.put("latmax", String.valueOf(latmax));
        params1.put("longmin", String.valueOf(longmin));
        params1.put("longmax", String.valueOf(longmax));

        Kumulos.call("searchCardsCDRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsCD();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("city", city_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsCDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsCD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("city", city_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsCDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsCD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsNFC(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        HashMap<String, String> params1 = new HashMap<String, String>();

        params1.put("daysOfWeek", dayOfWeek);
        params1.put("restaurantName", name_str);
        params1.put("food", food_str);
        params1.put("city", city_str);

        Kumulos.call("searchCardsNFCRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsNFC();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("food", food_str);
                    params2.put("city", city_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNFCSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsNFC();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("food", food_str);
                    params2.put("city", city_str);
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNFCSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsNFC();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsNFD(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        CalcParameters();

        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("daysOfWeek", dayOfWeek);
        params1.put("restaurantName", name_str);
        params1.put("food", food_str);
        params1.put("latmin", String.valueOf(latmin));
        params1.put("latmax", String.valueOf(latmax));
        params1.put("longmin", String.valueOf(longmin));
        params1.put("longmax", String.valueOf(longmax));

        Kumulos.call("searchCardsNFDRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsNFD();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("food", food_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNFDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsNFD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("food", food_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNFDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsNFD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsNCD(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        CalcParameters();

        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("daysOfWeek", dayOfWeek);
        params1.put("restaurantName", name_str);
        params1.put("city", city_str);
        params1.put("latmin", String.valueOf(latmin));
        params1.put("latmax", String.valueOf(latmax));
        params1.put("longmin", String.valueOf(longmin));
        params1.put("longmax", String.valueOf(longmax));

        Kumulos.call("searchCardsNCDRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsNCD();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("city", city_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNCDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsNCD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("city", city_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNCDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsNCD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsFCD(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        CalcParameters();

        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("daysOfWeek", dayOfWeek);
        params1.put("food", food_str);
        params1.put("city", city_str);
        params1.put("latmin", String.valueOf(latmin));
        params1.put("latmax", String.valueOf(latmax));
        params1.put("longmin", String.valueOf(longmin));
        params1.put("longmax", String.valueOf(longmax));

        Kumulos.call("searchCardsFCDRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsFCD();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("food", food_str);
                    params2.put("city", city_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsFCDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsFCD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("food", food_str);
                    params2.put("city", city_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsFCDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsFCD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void searchCardsNFCD(){
        clear();
        msg_tv.setVisibility(View.GONE);
        results_rv.setVisibility(View.VISIBLE);
        refresh_srl.setRefreshing(true);

        CalcParameters();

        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("daysOfWeek", dayOfWeek);
        params1.put("restaurantName", name_str);
        params1.put("food", food_str);
        params1.put("city", city_str);
        params1.put("latmin", String.valueOf(latmin));
        params1.put("latmax", String.valueOf(latmax));
        params1.put("longmin", String.valueOf(longmin));
        params1.put("longmax", String.valueOf(longmax));

        Kumulos.call("searchCardsNFCDRep",params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                searchCardsError++;
                if(searchCardsError>= 3){
                    searchCardsError = 0;
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    msg_tv.setVisibility(View.VISIBLE);
                    msg_tv.setText(getString(R.string.verify_connections));
                    return;
                }
                else{
                    searchCardsNFCD();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                if(objects.isEmpty()){
                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("food", food_str);
                    params2.put("city", city_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNFCDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsNFCD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
                else {
                    msg_tv.setVisibility(View.GONE);
                    results_rv.setVisibility(View.VISIBLE);
                    for(int i = 0; i < objects.size(); i++){
                        Searcheds s = new Searcheds();

                        s.c = new Cardy();
                        s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                        s.c.card = String.valueOf(objects.get(i).get("card"));

                        s.r = new Restaurant();
                        LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                        s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                        s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                        s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                        s.r.name = String.valueOf(restaurantMap.get("name"));
                        s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                        s.r.addr = String.valueOf(restaurantMap.get("addr"));
                        s.r.compl = String.valueOf(restaurantMap.get("compl"));
                        s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                        s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                        s.r.icon = String.valueOf(restaurantMap.get("icon"));

                        s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                        resultsList.add(s);
                    }

                    Collections.sort(resultsList, new Comparator<Searcheds>() {
                        @Override public int compare(Searcheds bo1, Searcheds bo2) {
                            return (bo1.distance >  bo2.distance ? 1:-1);
                        }
                    });

                    HashMap<String, String> params2 = new HashMap<>();
                    params2.put("restaurantName", name_str);
                    params2.put("food", food_str);
                    params2.put("city", city_str);
                    params2.put("latmin", String.valueOf(latmin));
                    params2.put("latmax", String.valueOf(latmax));
                    params2.put("longmin", String.valueOf(longmin));
                    params2.put("longmax", String.valueOf(longmax));
                    params2.put("date",String.valueOf(getNow().getTime()/1000));

                    Kumulos.call("searchCardsNFCDSin",params2, new ResponseHandler(){
                        @Override
                        public void onFailure(@Nullable Throwable error) {
                            searchCardsError++;
                            if(searchCardsError>= 3){
                                searchCardsError = 0;
                                Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                                msg_tv.setVisibility(View.VISIBLE);
                                msg_tv.setText(getString(R.string.verify_connections));
                                return;
                            }
                            else{
                                searchCardsNFCD();
                            }
                            super.onFailure(error);
                        }

                        @Override
                        public void didCompleteWithResult(@Nullable Object result) {
                            super.didCompleteWithResult(result);
                            ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;

                            if(objects.isEmpty()){
                                if(resultsList.isEmpty()) {
                                    logoLayout.setVisibility(View.VISIBLE);
                                    results_rv.setVisibility(View.GONE);
                                }
                                else{
                                    logoLayout.setVisibility(View.GONE);
                                    msg_tv.setVisibility(View.GONE);
                                    results_rv.setVisibility(View.VISIBLE);
                                    resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                    results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                    results_rv.setAdapter(resultsAdapter);
                                }
                            }
                            else {
                                logoLayout.setVisibility(View.GONE);
                                msg_tv.setVisibility(View.GONE);
                                results_rv.setVisibility(View.VISIBLE);
                                for(int i = 0; i < objects.size(); i++){
                                    Searcheds s = new Searcheds();

                                    s.c = new Cardy();
                                    s.c.cardID = String.valueOf(objects.get(i).get("cardID"));
                                    s.c.card = String.valueOf(objects.get(i).get("card"));

                                    s.r = new Restaurant();
                                    LinkedHashMap<String, Object> restaurantMap = (LinkedHashMap<String, Object>)objects.get(i).get("restaurant");
                                    s.r.telephone = String.valueOf(restaurantMap.get("telephone"));
                                    s.r.openTime = String.valueOf(restaurantMap.get("openTime"));
                                    s.r.closeTime = String.valueOf(restaurantMap.get("closeTime"));
                                    s.r.name = String.valueOf(restaurantMap.get("name"));
                                    s.r.fullAddr = String.valueOf(restaurantMap.get("fullAddr"));
                                    s.r.addr = String.valueOf(restaurantMap.get("addr"));
                                    s.r.compl = String.valueOf(restaurantMap.get("compl"));
                                    s.r.latitude = Double.parseDouble(String.valueOf(restaurantMap.get("latitude")));
                                    s.r.longitude = Double.parseDouble(String.valueOf(restaurantMap.get("longitude")));
                                    s.r.icon = String.valueOf(restaurantMap.get("icon"));

                                    s.distance = CalcDistance(s.r.latitude, s.r.longitude);

                                    resultsList.add(s);
                                }

                                Collections.sort(resultsList, new Comparator<Searcheds>() {
                                    @Override public int compare(Searcheds bo1, Searcheds bo2) {
                                        return (bo1.distance >  bo2.distance ? 1:-1);
                                    }
                                });

                                resultsAdapter = new ResultsAdapter(ctx,resultsList);
                                results_rv.addItemDecoration(new SimpleDividerItemDecoration(ctx));
                                results_rv.setAdapter(resultsAdapter);
                            }
                            refresh_srl.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.RViewHolder>{
        private List<Searcheds> mList;
        private LayoutInflater mLayoutInflater;
        private Context ctx;

        public ResultsAdapter(Context c, List<Searcheds> l){
            ctx = c;
            mList = l;
            mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public ResultsAdapter.RViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(R.layout.result_searched, parent, false);
            ResultsAdapter.RViewHolder mvh =  new ResultsAdapter.RViewHolder(view);
            return mvh;
        }

        @Override
        public void onBindViewHolder(ResultsAdapter.RViewHolder holder, int position) {
            holder.name_tv.setText(mList.get(position).r.name);
            holder.address_tv.setText(mList.get(position).r.fullAddr);
            holder.distance_tv.setText(String.format("%.2f",mList.get(position).distance) + " km");
            byte[] decodedString = Base64.decode(mList.get(position).r.icon, Base64.DEFAULT);
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
            public TextView distance_tv;
            public CardView cardView;

            public RViewHolder(View itemView) {
                super(itemView);

                name_tv = (TextView) itemView.findViewById(R.id.name_tv);
                address_tv = (TextView) itemView.findViewById(R.id.address_tv);
                image_img = (ImageView) itemView.findViewById(R.id.image_img);
                distance_tv = (TextView) itemView.findViewById(R.id.distance_tv);
                cardView = (CardView) itemView.findViewById(R.id.cardView);

                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        i = new Intent(ctx, CardActivity.class);
                        i.putExtra("CARD", mList.get(getAdapterPosition()).c.card);
                        i.putExtra("REST", mList.get(getAdapterPosition()).r.name);
                        i.putExtra("ADDRESS", mList.get(getAdapterPosition()).r.fullAddr);
                        i.putExtra("LATITUDE", String.valueOf(mList.get(getAdapterPosition()).r.latitude));
                        i.putExtra("LONGITUDE", String.valueOf(mList.get(getAdapterPosition()).r.longitude));
                        i.putExtra("TELEPHONE", mList.get(getAdapterPosition()).r.telephone);
                        i.putExtra("OPENTIME", mList.get(getAdapterPosition()).r.openTime);
                        i.putExtra("CLOSETIME", mList.get(getAdapterPosition()).r.closeTime);

                        if(adViewCount >= 3) {
                            if (mInterstitialAd.isLoaded()) {
                                adViewCount = 0;
                                setSetting("ADVIEWCOUNT","0");
                                mInterstitialAd.show();
                            } else {
                                startActivity(i);
                            }
                        }
                        else {
                            adViewCount++;
                            setSetting("ADVIEWCOUNT",String.valueOf(adViewCount));
                            startActivity(i);
                        }
                    }
                });
            }
        }
    }

    public void clear() {
        int size = this.resultsList.size();
        if(size > 0){
            this.resultsList.clear();
            resultsAdapter.notifyItemRangeRemoved(0, size);
        }
    }



}
