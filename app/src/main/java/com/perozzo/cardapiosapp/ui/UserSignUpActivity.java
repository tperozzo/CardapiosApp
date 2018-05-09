package com.perozzo.cardapiosapp.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.ContentFrameLayout;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.kumulos.android.Kumulos;
import com.kumulos.android.ResponseHandler;
import com.perozzo.cardapiosapp.R;
import com.perozzo.cardapiosapp.classes.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class UserSignUpActivity extends AppCompatActivity{

    public EditText email_et;
    public EditText password_et;
    public EditText cpassword_et;
    public TextView signup_btn;

    public Context ctx;
    public String email;
    public String password;
    public String cpassword;
    public boolean getUserOk = false;
    public boolean emailOk = false;
    public boolean passwordsOk = false;

    public int getUserError = 0;
    public int createUserError = 0;

    private SharedPreferences sharedPrefSettings;
    public ProgressDialog progressDialog;

    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setBackgroundDrawable(null);

        ctx = this;
        sharedPrefSettings = getSharedPreferences("CARDAPIOSAPP", 0);

        email_et = (EditText) findViewById(R.id.email_et);
        password_et = (EditText) findViewById(R.id.password_et);
        cpassword_et = (EditText) findViewById(R.id.cpassword_et);
        signup_btn = (TextView) findViewById(R.id.signup_btn);
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline()) {
                    doSignUp();
                }
                else{
                    Toast.makeText(getApplicationContext(), getString(R.string.noconnection), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void doSignUp(){
        validadeFields();
        if(emailOk && passwordsOk) {
            ProgressDialog();
            //searchUserOnDataBase();
            SignUpFirebase();
        }
    }

    public void validadeFields(){
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
        if(!password_et.getText().toString().isEmpty())
            password = password_et.getText().toString();
        else{
            Toast.makeText(ctx, getString(R.string.password_empty), Toast.LENGTH_LONG).show();
            return;
        }

        if(password.length() < 6){
            Toast.makeText(ctx, getString(R.string.password_6_char), Toast.LENGTH_LONG).show();
            return;
        }

        if(!cpassword_et.getText().toString().isEmpty())
            cpassword = cpassword_et.getText().toString();
        else{
            Toast.makeText(ctx, getString(R.string.cpassword_empty), Toast.LENGTH_LONG).show();
            return;
        }

        if(password.equals(cpassword)){
            passwordsOk = true;
        }
        else{
            Toast.makeText(ctx, getString(R.string.passwords_different), Toast.LENGTH_LONG).show();
            return;
        }
    }

    public void searchUserOnDataBase(){
        //2 - ver se o usuario nao existe
        if(emailOk && passwordsOk) {
            HashMap<String, String> params1 = new HashMap<String, String>();
            params1.put("email", email_et.getText().toString());
            Kumulos.call("getUser", params1, new ResponseHandler() {
                @Override
                public void onFailure(@Nullable Throwable error) {
                    getUserError++;
                    if(getUserError >= 2){
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
                    if (objects.isEmpty()) {
                        getUserOk = true;
                        createUser();
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(ctx, getString(R.string.user_already_exists), Toast.LENGTH_LONG).show();
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

    public void createUser(){
        //3 - Cadastra o usuário
        if(getUserOk){
            HashMap<String, String> params1 = new HashMap<String, String>();
            params1.put("email", email);
            params1.put("password", password);
            Kumulos.call("createUser", params1, new ResponseHandler() {
                @Override
                public void onFailure(@Nullable Throwable error) {
                    createUserError++;
                    if(createUserError >= 2){
                        createUserError = 0;
                        progressDialog.dismiss();
                        Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                        return;
                    }
                    else{
                        createUser();
                    }
                    super.onFailure(error);
                }

                @Override
                public void didCompleteWithResult(Object result) {
                    super.didCompleteWithResult(result);
                    //int objects = (int) result;
                    if (result != null) {
                        //ArrayList<LinkedHashMap<String, Object>> objects = (ArrayList<LinkedHashMap<String, Object>>) result;
                        Toast.makeText(ctx, getString(R.string.sign_up_successful), Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        //Vai para Activity Manage Rests
                        invalidateOptionsMenu();
                        Intent i = new Intent(UserSignUpActivity.this, ManageRestaurantsActivity.class);
                        i.putExtra("owner", String.valueOf(result));
                        i.putExtra("EMAIL", email);
                        i.putExtra("PASSWORD", password);
                        SaveCredentials();
                        startActivity(i);
                        finish();
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(ctx, getString(R.string.sign_up_failed), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            });
        }
    }

    public void SignUpFirebase(){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ctx, getString(R.string.sign_up_successful), Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    FirebaseUser user = mAuth.getCurrentUser();
                    user.sendEmailVerification();
                    invalidateOptionsMenu();
                    SaveCredentials();
                    finish();
                } else {

                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), getString(R.string.user_already_exists), Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.sign_up_failed), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    private void setSetting(String tag, String value) {
        try {
            SharedPreferences.Editor editor = sharedPrefSettings.edit();
            editor.putString(tag, value);
            editor.commit();
        } catch (Exception e) {
            Log.v("CardapiosApp", "err: " + e.getMessage());
        }
    }

    public void SaveCredentials(){ 
        setSetting("EMAIL", email);
        setSetting("PASSWORD", password);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
