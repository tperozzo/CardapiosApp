package com.perozzo.cardapiosapp.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.kumulos.android.Kumulos;
import com.kumulos.android.ResponseHandler;
import com.perozzo.cardapiosapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

//TODO Splash Activity, vai conter o logo e carregar em 3 segundos para a tela de scan ou de manageRest a partir das configs
//se tem login nas pref vai pra manageRest ou manageCardies se tiver só um rest
// se não tem login vai pra tela de Scan

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;
    public boolean isLogged = false;
    private SharedPreferences sharedPrefSettings;
    public String login = "";
    public String password = "";
    public int ID;
    public int contError = 0;
    final HashMap<String, String> params1 = new HashMap<String, String>();
    public String API_KEY = "157d9b9a-10e6-4d13-9a9a-c75a8100b46e";
    public String SECRET_KEY = "R9iSn9wVeS9nqkFzSVnwvMgZbRN3e2U3/hVl";
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setBackgroundDrawable(null);
        sharedPrefSettings = getSharedPreferences("CARDAPIOSAPP", 0);
        Kumulos.initWithAPIKeyAndSecretKey(API_KEY, SECRET_KEY, this);
        mAuth = FirebaseAuth.getInstance();
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.bordoDark));
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isOnline()) {
                    if (getCredencials()) {
                        LoginFirebase();
                    } else
                        goToScan();
                }
                else {
                    Toast.makeText(getApplicationContext(), getString(R.string.noconnection), Toast.LENGTH_SHORT).show();
                    goToScan();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private String getSetting(String tag, String defaultReturn) {
        try {
            return sharedPrefSettings.getString(tag, defaultReturn);
        } catch (Exception e) {
            Log.v("CardapiosApp", "err:"+e.getMessage());
            return defaultReturn;
        }
    }

    public boolean getCredencials(){
        login = getSetting("EMAIL","");
        password = getSetting("PASSWORD","");

        if(login.equals("")||password.equals(""))
            return false;
        else
            return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void LoginFirebase(){

        mAuth.signInWithEmailAndPassword(login, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if(mAuth.getCurrentUser().isEmailVerified()){
                        goToMagageRestaurants();
                    }
                    else{
                        goToScan();
                    }

                } else {
                    goToScan();
                }
            }
        });
    }

    public void goToMagageRestaurants(){
        Intent i = new Intent(SplashActivity.this, ManageRestaurantsActivity.class);
        startActivity(i);
        finish();
    }

    public void goToScan(){
        Intent i = new Intent(SplashActivity.this, Main2Activity.class);
        startActivity(i);
        finish();
    }

}