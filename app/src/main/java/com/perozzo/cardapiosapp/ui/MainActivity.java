package com.perozzo.cardapiosapp.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.kumulos.android.Kumulos;
import com.kumulos.android.ResponseHandler;
import com.perozzo.cardapiosapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public boolean isLogged = false;
    public Context ctx;
    public String email, password, userID;
    private SharedPreferences sharedPrefSettings;
    Toolbar toolbar;

    public GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    public static double latitude, longitude;
    public static double latmin, latmax, longmin, longmax;
    public boolean locationPermission = false;

    public int loginError = 0;

    public InterstitialAd mInterstitialAd;
    public int adViewCount;
    public Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        sharedPrefSettings = getSharedPreferences("CARDAPIOSAPP", 0);
        ctx = this;
        email = getSetting("EMAIL", "");
        password = getSetting("PASSWORD", "");
        userID = getIntent().getStringExtra("owner");

        LocationPermission();

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

        ProgressDialog();
        login();
    }

    public void login() {
        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("email", email);
        params1.put("password", password);
        Kumulos.call("login", params1, new ResponseHandler() {
            @Override
            public void onFailure(@Nullable Throwable error) {
                //  Log.v("LOGINERROR",String.valueOf(loginError));
                loginError++;
                if(loginError >= 2){
                    loginError = 0;
                    progressDialog.dismiss();
                    MakeDrawer();
                    return;
                }
                else{
                    login();
                }
                super.onFailure(error);
                return;
            }

            @Override
            public void didFailWithError(String message) {
                Log.v("LOGINERROR","FAIL");
                super.didFailWithError(message);

            }

            @Override
            public void didCompleteWithResult(Object result) {
                super.didCompleteWithResult(result);
                ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;
                if (!objects.isEmpty()) {
                    invalidateOptionsMenu();
                    int ID = (int) objects.get(0).get("userID");
                    isLogged = true;
                }
                progressDialog.dismiss();
                MakeDrawer();
            }
        });
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

    private String getSetting(String tag, String defaultReturn) {
        try {
            return sharedPrefSettings.getString(tag, defaultReturn);
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

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
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
            Intent i = new Intent(MainActivity.this, UserLoginActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_signup) {
            //start signUp Act
            invalidateOptionsMenu();
            Intent i = new Intent(MainActivity.this, UserSignUpActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_manage_rests) {
            Intent i = new Intent(MainActivity.this, ManageRestaurantsActivity.class);
            i.putExtra("owner", userID);
            i.putExtra("EMAIL", email);
            i.putExtra("PASSWORD", password);
            startActivity(i);
            finish();
        } else if (id == R.id.nav_logout) {
            LogOut();
            isLogged = false;
            MakeDrawer();
        }else if(id == R.id.nav_change_account){
            Intent i = new Intent(MainActivity.this, UserSettings.class);
            i.putExtra("EMAIL",email);
            i.putExtra("PASSWORD",password);
            i.putExtra("owner",userID);
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
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("email", email);
        params.put("password", password);
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
                super.didCompleteWithResult(result);
            }


        });
    }

    public void LogOut() {
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

    public static void CalcParameters(){
        latmin = latitude - 0.5;//0.18;
        latmax = latitude + 0.5;//0.18;
        longmin = longitude - 0.50;//.2*(Math.cos(latitude));
        longmax = longitude + 0.5;//0.2*(Math.cos(latitude));

        //TODO in fragments
        //search_btn.setEnabled(true);
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
        }
        else{
            //TODO TOAST msg_tv.setText("Localização não encontrada. \nVerifique sua conexão e suas permissões.");
        }
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


}
