package com.perozzo.cardapiosapp.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kumulos.android.Kumulos;
import com.kumulos.android.ResponseHandler;
import com.perozzo.cardapiosapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class UserLoginActivity extends AppCompatActivity {

    public EditText email_et;
    public EditText password_et;
    public TextView login_btn;

    public Context ctx;
    public String email;
    public String password;
    public boolean getUserOk = false;
    public boolean emailOk = false;
    public boolean passwordOk = false;
    private SharedPreferences sharedPrefSettings;
    public ProgressDialog progressDialog;

    public int getUserError = 0;
    public int loginError = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setBackgroundDrawable(null);

        sharedPrefSettings = getSharedPreferences("CARDAPIOSAPP", 0);
        ctx = this;

        email_et = (EditText) findViewById(R.id.email_et);
        password_et = (EditText) findViewById(R.id.password_et);
        login_btn = (TextView) findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline()) {
                    doLogin();
                }
                else{
                    Toast.makeText(getApplicationContext(), getString(R.string.noconnection), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void doLogin(){
        verifyFields();
        if(emailOk && passwordOk) {
            ProgressDialog();
            searchUserOnDataBase();
        }
    }

    public void verifyFields(){
        //1 - validar campos
        //1.1 - email : tem que ser não nulo, tem que estar no Patters
        if(!email_et.getText().toString().isEmpty())
            email = email_et.getText().toString();
        else{
            Toast.makeText(ctx, getString(R.string.email_empty), Toast.LENGTH_LONG).show();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(ctx, getString(R.string.email_invalid), Toast.LENGTH_LONG).show();
            return;
        }
        else{
            emailOk = true;
        }

        //1.2 - passwords : tem que ser não nulo, tem que ser iguais
        if(!password_et.getText().toString().isEmpty()) {
            password = password_et.getText().toString();
            passwordOk = true;
        }
        else{
            Toast.makeText(ctx, getString(R.string.password_empty), Toast.LENGTH_LONG).show();
            return;
        }


    }

    public void searchUserOnDataBase(){
        //2 - ver se o usuario nao existe
        if(emailOk && passwordOk) {
            HashMap<String, String> params1 = new HashMap<String, String>();
            params1.put("email", email_et.getText().toString());
            Kumulos.call("getUser", params1, new ResponseHandler() {
                @Override
                public void onFailure(@Nullable Throwable error) {
                    getUserError++;
                    if(getUserError >= 3){
                        getUserError = 0;
                        progressDialog.dismiss();
                        Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                        return;
                    }
                    else{
                        searchUserOnDataBase();
                    }
                    super.onFailure(error);
                }

                @Override
                public void didCompleteWithResult(Object result) {
                    super.didCompleteWithResult(result);
                    ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;
                    if (!objects.isEmpty()) {
                        getUserOk = true;
                        login();
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(ctx, getString(R.string.user_does_not_exists), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            });
        }
    }

    public void ProgressDialog(){
        progressDialog = new ProgressDialog(ctx);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.empty_progress_dialog);
        progressDialog.setIndeterminate(true);
    }

    public void login(){
        if(getUserOk){
            HashMap<String, String> params1 = new HashMap<String, String>();
            params1.put("email", email);
            params1.put("password", password);
            Kumulos.call("login", params1, new ResponseHandler() {
                @Override
                public void onFailure(@Nullable Throwable error) {
                    loginError++;
                    if(loginError >= 3){
                        loginError = 0;
                        progressDialog.dismiss();
                        Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                        return;
                    }
                    else{
                        login();
                    }
                    super.onFailure(error);
                }

                @Override
                public void didCompleteWithResult(Object result) {
                    super.didCompleteWithResult(result);
                    ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String,Object>>) result;
                    if (!objects.isEmpty()) {
                        Toast.makeText(ctx, getString(R.string.login_successful), Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        invalidateOptionsMenu();
                        int ID = (int)objects.get(0).get("userID");
                        Intent i = new Intent(UserLoginActivity.this, ManageRestaurantsActivity.class);
                        i.putExtra("owner",String.valueOf(ID));
                        i.putExtra("EMAIL", email);
                        i.putExtra("PASSWORD", password);

                        SaveCredentials();

                        startActivity(i);
                        finish();
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(ctx, getString(R.string.password_invalid), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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

    public void SaveCredentials(){
        setSetting("EMAIL", email);
        setSetting("PASSWORD", password);
    }
}
