package com.perozzo.cardapiosapp.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kumulos.android.Kumulos;
import com.kumulos.android.ResponseHandler;
import com.perozzo.cardapiosapp.R;
import java.util.HashMap;

public class UserSettings extends AppCompatActivity {

    public EditText email_et;
    public EditText password_et;
    public EditText cpassword_et;
    public TextView update_btn;

    public String ID, email, password, cpassword;
    public Context ctx;
    public boolean emailOk = false, passwordsOk = false;

    public int updateUserError = 0;
    private SharedPreferences sharedPrefSettings;
    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setBackgroundDrawable(null);

        ctx = this;
        sharedPrefSettings = getSharedPreferences("CARDAPIOSAPP", 0);

        ID = getIntent().getStringExtra("owner");
        email = getIntent().getStringExtra("EMAIL");
        password = getIntent().getStringExtra("PASSWORD");

        email_et = (EditText) findViewById(R.id.email_et);
        email_et.setText(email);
        password_et = (EditText) findViewById(R.id.password_et);
        password_et.setText(password);
        cpassword_et = (EditText) findViewById(R.id.cpassword_et);

        update_btn = (TextView) findViewById(R.id.update_btn);

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline()){
                    validadeFields();
                    if(emailOk && passwordsOk) {
                        ProgressDialog();
                        updateUser();
                    }
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

    public void ProgressDialog(){
        progressDialog = new ProgressDialog(ctx);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.empty_progress_dialog);
        progressDialog.setIndeterminate(true);
    }

    public void updateUser(){
        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("email", email);
        params1.put("password", password);
        params1.put("userID", ID);
        Kumulos.call("updateUser", params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                updateUserError++;
                if(updateUserError >= 3){
                    updateUserError = 0;
                    progressDialog.dismiss();
                    Toast.makeText(ctx, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    updateUser();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                if((int)result == 1) {
                    Toast.makeText(ctx, getString(R.string.updated_user), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    SaveCredentials();
                    finish();
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(ctx, getString(R.string.updated_user_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
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
