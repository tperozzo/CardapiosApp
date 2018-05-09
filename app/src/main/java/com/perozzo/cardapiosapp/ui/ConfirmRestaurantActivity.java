package com.perozzo.cardapiosapp.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.kumulos.android.Kumulos;
import com.kumulos.android.ResponseHandler;
import com.perozzo.cardapiosapp.R;
import com.perozzo.cardapiosapp.classes.Restaurant;

import java.util.HashMap;

public class ConfirmRestaurantActivity extends AppCompatActivity {

    public boolean isAdd = false;
    public Restaurant r;
    public TextView name_tv, address_tv;
    public ImageView image_img;
    public Context mContext;

    public int createRestaurantError = 0;
    public int updateRestaurantError = 0;

    public HashMap<String, String> params1 = new HashMap<String, String>();
    private SharedPreferences sharedPrefSettings;
    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_restaurant);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        getWindow().setBackgroundDrawable(null);

        mContext = this;
        sharedPrefSettings = getSharedPreferences("CARDAPIOSAPP", 0);

        name_tv = (TextView) findViewById(R.id.name_tv);
        address_tv = (TextView) findViewById(R.id.address_tv);
        image_img = (ImageView) findViewById(R.id.image_img);

        r = (Restaurant) getIntent().getSerializableExtra("Restaurant");
        r.icon = getSetting("PICTUREDATA","");
        isAdd = getIntent().getBooleanExtra("isAdd",false);
        setSupportActionBar(toolbar);

        name_tv.setText(r.name);
        address_tv.setText(r.fullAddr + ", "+r.city);
        byte[] decodedString = Base64.decode(r.icon, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        image_img.setImageDrawable(null);
        image_img.setImageBitmap(decodedByte);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isAdd) {
                    if (isOnline()){
                        ProgressDialog();
                        createRestaurant();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), getString(R.string.noconnection), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    if(isOnline()) {
                        ProgressDialog();
                        updateRestaurant();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), getString(R.string.noconnection), Toast.LENGTH_SHORT).show();
                    }
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

    public void createRestaurant(){
        params1 = new HashMap<String, String>();
        params1.put("byCoords", r.byCoords);
        params1.put("restaurantID",r.restaurantID);
        params1.put("icon",r.icon);
        params1.put("openTime",r.openTime);
        params1.put("closeTime",r.closeTime);
        params1.put("telephone",r.telephone);
        params1.put("name",r.name);
        params1.put("fullAddr",r.fullAddr);
        params1.put("latitude",String.valueOf(r.latitude));
        params1.put("longitude",String.valueOf(r.longitude));
        params1.put("owner",r.owner);
        params1.put("city",r.city);
        params1.put("compl",r.compl);
        params1.put("addr",r.addr);
        Kumulos.call("createRestaurant", params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                createRestaurantError++;
                if(createRestaurantError>= 2){
                    createRestaurantError = 0;
                    Toast.makeText(mContext, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    return;
                }
                else{
                    createRestaurant();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(Object result) {
                super.didCompleteWithResult(result);
                if((int)result > -1) {
                    Toast.makeText(mContext, getString(R.string.rest_created), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    AddEditRestaurantsActivity.me.finish();
                    finish();
                }
                else{
                    Toast.makeText(mContext, getString(R.string.rest_created_fail), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    public void ProgressDialog(){
        progressDialog = new ProgressDialog(mContext);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.empty_progress_dialog);
        progressDialog.setIndeterminate(true);
    }

    public void updateRestaurant(){
        Kumulos.call("updateRestaurant", params1, new ResponseHandler(){
            @Override
            public void onFailure(@Nullable Throwable error) {
                updateRestaurantError++;
                if(updateRestaurantError>= 2){
                    updateRestaurantError = 0;
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    updateRestaurant();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(Object result) {
                super.didCompleteWithResult(result);
                if((int)result == 1) {
                    Toast.makeText(mContext, getString(R.string.rest_updated), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    AddEditRestaurantsActivity.me.finish();
                    finish();
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.rest_updated_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private String getSetting(String tag, String defaultReturn) {
        try {
            return sharedPrefSettings.getString(tag, defaultReturn);
        } catch (Exception e) {
            return defaultReturn;
        }
    }
}
