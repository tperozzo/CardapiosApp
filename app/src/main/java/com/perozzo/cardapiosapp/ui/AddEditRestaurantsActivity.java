package com.perozzo.cardapiosapp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;


import com.kumulos.android.Kumulos;
import com.kumulos.android.ResponseHandler;
import com.perozzo.cardapiosapp.R;
import com.perozzo.cardapiosapp.classes.Restaurant;
import com.perozzo.cardapiosapp.components.AddressSearchAdapter;
import com.perozzo.cardapiosapp.components.CitySearchAdapter;
import com.perozzo.cardapiosapp.components.DelayAutoCompleteTextView;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class AddEditRestaurantsActivity extends AppCompatActivity {

    public boolean workFine = false;
    public Context mContext;
    private Integer THRESHOLD = 2;
    public ImageView imageView;
    public EditText name_et;
    public DelayAutoCompleteTextView city_et;
    public DelayAutoCompleteTextView address_et;
    public EditText compl_et;
    public EditText latitude_et, longitude_et;
    public Address address;
    public EditText telephone_et, openTime_et, closeTime_et;
    public boolean isAdd = false;
    public boolean cityIsValid = false;
    public boolean addrIsValid = false;
    public String owner;
    public Restaurant r, temp;
    public static Activity me;
    public int removeRestaurantError = 0;
    public String city;
    public String pictureData = "";
    public CardView cv;
    private SharedPreferences sharedPrefSettings;
    public ProgressDialog progressDialog;
    public boolean byCoordinates = false;
    public CheckBox edit_coord_cb;
    public boolean isInit = true;
    public List<Address> addresses = null;
    public FloatingActionButton img_fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_restaurants);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setBackgroundDrawable(null);
        me = this;

        sharedPrefSettings = getSharedPreferences("CARDAPIOSAPP", 0);
        pictureData = getSetting("PICTUREDATA","");

        owner = getIntent().getStringExtra("owner");
        r = (Restaurant) getIntent().getSerializableExtra("Restaurant");

        if(r == null)
            isAdd = true;
        else
            isAdd = false;

        mContext = this;

        cv = (CardView) findViewById(R.id.info_cv);

        imageView = (ImageView)findViewById(R.id.image_img);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        img_fab = (FloatingActionButton) findViewById(R.id.img_fab);
        img_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent , 0 );
            }
        });

        edit_coord_cb = (CheckBox) findViewById(R.id.coord_cb);
        edit_coord_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(edit_coord_cb.isChecked()){
                    latitude_et.setEnabled(true);
                    longitude_et.setEnabled(true);
                    byCoordinates = true;
                }
                else{
                    latitude_et.setEnabled(false);
                    longitude_et.setEnabled(false);
                    byCoordinates = false;
                }
            }
        });

        name_et = (EditText)findViewById(R.id.name_et);
        telephone_et = (EditText)findViewById(R.id.telephone_et);
        openTime_et = (EditText)findViewById(R.id.openTime_et);
        openTime_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowTimePicker(true);
            }
        });
        closeTime_et = (EditText)findViewById(R.id.closeTime_et);
        closeTime_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowTimePicker(false);
            }
        });
        latitude_et = (EditText) findViewById(R.id.latitude_et);
        longitude_et = (EditText) findViewById(R.id.longitude_et);

        if(isAdd) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            Resources r = mContext.getResources();
            int px  = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    96,
                    r.getDisplayMetrics()
            );

            layoutParams.setMargins(0, 0, 0, px);
            cv.setLayoutParams(layoutParams);

        }
        else{
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            Resources r = mContext.getResources();
            int px  = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    168,
                    r.getDisplayMetrics()
            );

            layoutParams.setMargins(0, 0, 0, px);
            cv.setLayoutParams(layoutParams);

        }

        city_et = (DelayAutoCompleteTextView) findViewById(R.id.city_et);
        city_et.setThreshold(THRESHOLD);
        city_et.setAdapter(new CitySearchAdapter(this));
        city_et.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CitySearchResult result = (CitySearchResult) parent.getItemAtPosition(position);
                city_et.setText(result.toEditText());
                //cityIsValid = true;
                //city_et.setSelection(city_et.getText().length());
                if(!result.addressString.equals("")) {
                    address_et.setEnabled(true);
                    address_et.requestFocus();
                    address_et.setThreshold(THRESHOLD);
                    address_et.setAdapter(new AddressSearchAdapter(mContext, result.addressString));
                    city = result.addressString;
                }
                else{
                    //cityIsValid = false;
                    address_et.setEnabled(false);
                }
            }
        });

        address_et = (DelayAutoCompleteTextView) findViewById(R.id.address_et);
        address_et.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AddressSearchResult result = (AddressSearchResult) parent.getItemAtPosition(position);
                address_et.setText(result.toEditText());
                address = result.address;
                compl_et.requestFocus();
            }
        });

        address_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if((!hasFocus)&&(address_et.getText().toString().length() > 0)&&(!byCoordinates)){
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected void onPreExecute() {
                            ProgressDialog();
                            super.onPreExecute();
                        }

                        @Override
                        protected Void doInBackground(Void... params) {
                            DoWork2();
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            progressDialog.dismiss();
                            super.onPostExecute(aVoid);
                        }
                    }.execute();

                }
                else{

                }
            }
        });

        compl_et = (EditText) findViewById(R.id.compl_et);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if(isOnline()) {

                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected void onPreExecute() {
                                ProgressDialog();

                                super.onPreExecute();
                            }

                            @Override
                            protected Void doInBackground(Void... params) {
                                DoWork();
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void result) {
                                progressDialog.dismiss();
                                if (workFine) {
                                    Intent i = new Intent(AddEditRestaurantsActivity.this, ConfirmRestaurantActivity.class);
                                    i.putExtra("isAdd", isAdd);
                                    i.putExtra("Restaurant", temp);
                                    startActivity(i);
                                }
                            }
                        }.execute();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), getString(R.string.noconnection), Toast.LENGTH_SHORT).show();
                    }

                }

        });

        FloatingActionButton delete_fab = (FloatingActionButton) findViewById(R.id.delete_fab);
        delete_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDeleteConfirmDialog();
            }
        });



        if(!isAdd){
            if(r.byCoords.equals("true"))
                edit_coord_cb.setChecked(true);
            else
                edit_coord_cb.setChecked(false);

            delete_fab.setVisibility(View.VISIBLE);
            telephone_et.setText(r.telephone);
            openTime_et.setText(r.openTime);
            closeTime_et.setText(r.closeTime);
            compl_et.setText(r.compl);
            address_et.setText(r.addr);
            address_et.setEnabled(true);
            name_et.setText(r.name);
            city_et.setText(r.city);
            latitude_et.setText(String.valueOf(r.latitude));
            longitude_et.setText(String.valueOf(r.longitude));

            byte[] decodedString = Base64.decode(pictureData, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedByte);
        }
        else{
            edit_coord_cb.setChecked(false);
            delete_fab.setVisibility(View.GONE);
        }


    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void ProgressDialog(){
        progressDialog = new ProgressDialog(mContext);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.empty_progress_dialog);
        progressDialog.setIndeterminate(true);
    }

    public void DoWork() {

        if(name_et.getText().toString().equals("")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext,getString(R.string.invalid_name),Toast.LENGTH_SHORT).show();
                }
            });

            workFine = false;
            return;
        }

        else if(city_et.getText().toString().equals("")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext,getString(R.string.invalid_city),Toast.LENGTH_SHORT).show();
                }
            });

            workFine = false;
            return;
        }
        else if(address_et.getText().toString().equals("")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext,getString(R.string.invalid_address),Toast.LENGTH_SHORT).show();
                }
            });

            workFine = false;
            return;
        }
        else if((latitude_et.getText().toString().equals(""))||
                (Double.parseDouble(latitude_et.getText().toString()) > 90)||(Double.parseDouble(latitude_et.getText().toString()) < -90)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext,getString(R.string.invalid_latitude),Toast.LENGTH_SHORT).show();
                }
            });

            workFine = false;
            return;
        }
        else if((longitude_et.getText().toString().equals(""))||
                (Double.parseDouble(longitude_et.getText().toString()) > 180)||(Double.parseDouble(longitude_et.getText().toString()) < -180)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext,getString(R.string.invalid_longitude),Toast.LENGTH_SHORT).show();
                }
            });

            workFine = false;
            return;
        }

        if(byCoordinates){
            temp = new Restaurant();
            if(edit_coord_cb.isChecked())
                temp.byCoords = "1";
            else
                temp.byCoords = "0";

            if(openTime_et.getText().toString().length() == 0){
                temp.openTime = "11:00";
            }
            else{
                temp.openTime = openTime_et.getText().toString();
            }

            if(closeTime_et.getText().toString().length() == 0){
                temp.closeTime = "15:00";
            }
            else{
                temp.closeTime = closeTime_et.getText().toString();
            }

            temp.telephone = telephone_et.getText().toString();
            temp.name = name_et.getText().toString();
            temp.addr = address_et.getText().toString();
            temp.city = city_et.getText().toString();
            temp.compl = compl_et.getText().toString();
            temp.latitude = Double.parseDouble(latitude_et.getText().toString());
            temp.longitude = Double.parseDouble(longitude_et.getText().toString());
            temp.owner = owner;
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            Bitmap bm = imageView.getDrawingCache();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            String img_str = Base64.encodeToString(byteArray, 0);

            pictureData = img_str;
            SavePictureData();
            imageView.destroyDrawingCache();
            if (!isAdd)
                temp.restaurantID = r.restaurantID;

            if (!temp.compl.equals(""))
                temp.fullAddr = temp.addr + ", " + temp.compl;
            else
                temp.fullAddr = temp.addr;

            workFine = true;

        }
        else {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(address_et.getText().toString() + " " + city, 15);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses.size() == 0) {
                try {
                    addresses = geocoder.getFromLocationName(address_et.getText().toString(), 15);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if ((city_et.getText().toString() != null) && (address_et.getText().toString() != null) && (addresses.size() > 0)) {

                temp = new Restaurant();
                if(edit_coord_cb.isChecked())
                    temp.byCoords = "1";
                else
                    temp.byCoords = "0";

                if(openTime_et.getText().toString().length() == 0){
                    temp.openTime = "11:00";
                }
                else{
                    temp.openTime = openTime_et.getText().toString();
                }

                if(closeTime_et.getText().toString().length() == 0){
                    temp.closeTime = "15:00";
                }
                else{
                    temp.closeTime = closeTime_et.getText().toString();
                }

                temp.telephone = telephone_et.getText().toString();
                temp.name = name_et.getText().toString();
                temp.addr = address_et.getText().toString();
                temp.city = addresses.get(0).getLocality();
                temp.compl = compl_et.getText().toString();
                temp.latitude = Double.parseDouble(latitude_et.getText().toString());
                temp.longitude = Double.parseDouble(longitude_et.getText().toString());
                temp.owner = owner;
                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
                Bitmap bm = imageView.getDrawingCache();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                String img_str = Base64.encodeToString(byteArray, 0);
                //temp.icon = img_str;
                pictureData = img_str;
                SavePictureData();
                imageView.destroyDrawingCache();
                if (!isAdd)
                    temp.restaurantID = r.restaurantID;
                // r.restaurantID =
                if (!temp.compl.equals(""))
                    temp.fullAddr = temp.addr + ", " + temp.compl;
                else
                    temp.fullAddr = temp.addr;

                workFine = true;
            }
            else{
                workFine = false;
            }
        }
    }

    public void DoWork2(){
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        addresses = null;
        try {
            addresses = geocoder.getFromLocationName(address_et.getText().toString() + " " + city, 15);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses.size() == 0) {
            try {
                addresses = geocoder.getFromLocationName(address_et.getText().toString(), 15);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (addresses.size() == 0) {
            Toast.makeText(mContext, getString(R.string.invalid_address), Toast.LENGTH_SHORT).show();
        } else {
            if(!byCoordinates) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        latitude_et.setText(String.valueOf(addresses.get(0).getLatitude()));
                        longitude_et.setText(String.valueOf(addresses.get(0).getLongitude()));
                    }
                });

            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);
        }
    }

    public void removeRestaurant(){
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("restaurantID", r.restaurantID);
        params.put("owner", r.owner);
        Kumulos.call("removeRestaurant", params, new ResponseHandler() {
            @Override
            public void onFailure(@Nullable Throwable error) {
                removeRestaurantError++;
                if(removeRestaurantError>= 3){
                    removeRestaurantError = 0;
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    removeRestaurant();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                if((int)result == 1) {
                    progressDialog.dismiss();
                    Toast.makeText(mContext, "Restaurante excluído!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(mContext, "Não foi possível excluir o restaurante.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        finish();
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            imageView.setImageDrawable(null);
            String s = Crop.getOutput(result).getPath();
            imageView.setImageBitmap(rotateBitmap(s));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Rotate a bitmap based on orientation metadata.
     * src - image path
     */
    public static Bitmap rotateBitmap(String src) {
        Bitmap bitmap = BitmapFactory.decodeFile(src);
        try {
            ExifInterface exif = new ExifInterface(src);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                case ExifInterface.ORIENTATION_UNDEFINED:
                default:
                    return bitmap;
            }

            try {
                Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return oriented;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
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

    private String getSetting(String tag, String defaultReturn) {
        try {
            return sharedPrefSettings.getString(tag, defaultReturn);
        } catch (Exception e) {
            return defaultReturn;
        }
    }

    public void SavePictureData(){
        setSetting("PICTUREDATA", pictureData);
    }

    AlertDialog dialog;
    public void ShowDeleteConfirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Tem certeza que deseja deletar o restaurante?")
                .setTitle("Deletar Restaurante");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                if(isOnline()) {
                    ProgressDialog();
                    removeRestaurant();
                }
                else{
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

    int hour;
    int minutes;
    public AlertDialog dialog2;
    public void ShowTimePicker(final boolean isOpen){
        Calendar cal = Calendar.getInstance();
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minutes = cal.get(Calendar.MINUTE);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View dialogView = inflater.inflate(R.layout.time_dialog, null);
        final TimePicker tp = (TimePicker) dialogView.findViewById(R.id.time_picker);
        //TextView ok_btn = (TextView) dialogView.findViewById(R.id.ok_btn);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(isOpen){
                    openTime_et.setText(String.format("%02d",hour)+":"+String.format("%02d",minutes));
                }
                else{
                    closeTime_et.setText(String.format("%02d",hour)+":"+String.format("%02d",minutes));
                }
                dialog2.dismiss();
            }
        });


        builder.setView(dialogView);
        dialog2 = builder.create();
        dialog2.show();

        tp.setIs24HourView(true);

        tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                hour = hourOfDay;
                minutes = minute;
            }
        });


/*
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOpen){
                    openTime_et.setText(String.format("%02d",hour)+":"+String.format("%02d",minutes));
                }
                else{
                    closeTime_et.setText(String.format("%02d",hour)+":"+String.format("%02d",minutes));
                }
                alertDialog.dismiss();
            }
        });*/
    }

}
