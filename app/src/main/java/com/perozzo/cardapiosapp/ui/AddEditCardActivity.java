package com.perozzo.cardapiosapp.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.kumulos.android.Kumulos;
import com.kumulos.android.ResponseHandler;
import com.perozzo.cardapiosapp.R;
import com.perozzo.cardapiosapp.classes.Cardy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class AddEditCardActivity extends AppCompatActivity {

    public Context mContext;
    public CardView repeat_cv, single_cv;
    public CheckBox single_cb, repeat_cb;
    public EditText date_et;
    public TextView sun_tv, mon_tv, tue_tv, wed_tv, thu_tv, fri_tv, sat_tv;
    public EditText foods_et;
    public Date data;
    public Date now;
    public String restaurantID;
    public boolean isAdd = false;
    public double latitude, longitude;
    public Cardy c;

    public int getCardByDateError = 0;
    public int createCardError = 0;
    public int updateCardError = 0;
    public int removeCardError = 0;

    public ProgressDialog progressDialog;
    public InterstitialAd mInterstitialAd;
    public String name;
    public String city;

    ArrayList<LinkedHashMap<String, Object>> objects1 = null;

    public CardView cv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_cardy);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setBackgroundDrawable(null);

        mContext = this;
        restaurantID = getIntent().getStringExtra("restaurantID");
        isAdd = getIntent().getBooleanExtra("isADD", false);
        c = (Cardy) getIntent().getSerializableExtra("CARD");
        if(c == null)
            c = new Cardy();
        longitude = getIntent().getDoubleExtra("LONGITUDE", 0);
        latitude = getIntent().getDoubleExtra("LATITUDE", 0);
        name = getIntent().getStringExtra("NAME");
        city = getIntent().getStringExtra("CITY");

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                finish();
            }
        });

        requestNewInterstitial();

        single_cv = (CardView) findViewById(R.id.single_cv);
        single_cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                single_cb.setChecked(true);
                repeat_cb.setChecked(false);
                repeat_cv.setCardBackgroundColor(getResources().getColor(R.color.grey));
                single_cv.setCardBackgroundColor(getResources().getColor(R.color.white));
                date_et.setEnabled(true);
                sun_tv.setEnabled(false);
                sun_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                sun_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                mon_tv.setEnabled(false);
                mon_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                mon_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                tue_tv.setEnabled(false);
                tue_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                tue_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                wed_tv.setEnabled(false);
                wed_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                wed_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                thu_tv.setEnabled(false);
                thu_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                thu_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                fri_tv.setEnabled(false);
                fri_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                fri_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                sat_tv.setEnabled(false);
                sat_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                sat_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
            }
        });
        single_cb = (CheckBox) findViewById(R.id.single_cb);
        single_cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                single_cb.setChecked(true);
                repeat_cb.setChecked(false);
                repeat_cv.setCardBackgroundColor(getResources().getColor(R.color.grey));
                single_cv.setCardBackgroundColor(getResources().getColor(R.color.white));
                date_et.setEnabled(true);
                sun_tv.setEnabled(false);
                sun_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                sun_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                mon_tv.setEnabled(false);
                mon_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                mon_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                tue_tv.setEnabled(false);
                tue_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                tue_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                wed_tv.setEnabled(false);
                wed_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                wed_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                thu_tv.setEnabled(false);
                thu_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                thu_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                fri_tv.setEnabled(false);
                fri_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                fri_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                sat_tv.setEnabled(false);
                sat_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                sat_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
            }
        });
        repeat_cv = (CardView) findViewById(R.id.repeat_cv);
        repeat_cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeat_cb.setChecked(true);
                single_cb.setChecked(false);
                single_cv.setCardBackgroundColor(getResources().getColor(R.color.grey));
                repeat_cv.setCardBackgroundColor(getResources().getColor(R.color.white));
                date_et.setEnabled(false);
                sun_tv.setEnabled(true);
                if(c.daysOfWeek.contains("sun")){
                    sun_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    sun_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                mon_tv.setEnabled(true);
                if(c.daysOfWeek.contains("mon")){
                    mon_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    mon_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                tue_tv.setEnabled(true);
                if(c.daysOfWeek.contains("tue")){
                    tue_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    tue_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                wed_tv.setEnabled(true);
                if(c.daysOfWeek.contains("wed")){
                    wed_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    wed_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                thu_tv.setEnabled(true);
                if(c.daysOfWeek.contains("thu")){
                    thu_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    thu_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                fri_tv.setEnabled(true);
                if(c.daysOfWeek.contains("fri")) {
                    fri_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    fri_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                sat_tv.setEnabled(true);
                if(c.daysOfWeek.contains("sat")) {
                    sat_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    sat_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
            }
        });
        repeat_cb = (CheckBox) findViewById(R.id.repeat_cb);
        repeat_cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeat_cb.setChecked(true);
                single_cb.setChecked(false);
                single_cv.setCardBackgroundColor(getResources().getColor(R.color.grey));
                repeat_cv.setCardBackgroundColor(getResources().getColor(R.color.white));
                date_et.setEnabled(false);
                sun_tv.setEnabled(true);
                if(c.daysOfWeek.contains("sun")){
                    sun_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    sun_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                mon_tv.setEnabled(true);
                if(c.daysOfWeek.contains("mon")){
                    mon_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    mon_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                tue_tv.setEnabled(true);
                if(c.daysOfWeek.contains("tue")){
                    tue_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    tue_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                wed_tv.setEnabled(true);
                if(c.daysOfWeek.contains("wed")){
                    wed_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    wed_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                thu_tv.setEnabled(true);
                if(c.daysOfWeek.contains("thu")){
                    thu_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    thu_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                fri_tv.setEnabled(true);
                if(c.daysOfWeek.contains("fri")) {
                    fri_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    fri_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                sat_tv.setEnabled(true);
                if(c.daysOfWeek.contains("sat")) {
                    sat_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    sat_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
            }
        });
        date_et = (EditText) findViewById(R.id.date_et);
        date_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        sun_tv = (TextView) findViewById(R.id.sun_tv);
        sun_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c.daysOfWeek.contains("sun")) {
                    c.daysOfWeek = c.daysOfWeek.replace("sun","");
                    sun_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                    sun_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));

                }
                else{
                    c.daysOfWeek += "sun";
                    sun_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    sun_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
            }
        });
        mon_tv = (TextView) findViewById(R.id.mon_tv);
        mon_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c.daysOfWeek.contains("mon")) {
                    c.daysOfWeek = c.daysOfWeek.replace("mon","");
                    mon_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                    mon_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                }
                else{
                    c.daysOfWeek += "mon";
                    mon_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    mon_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
            }
        });
        tue_tv = (TextView) findViewById(R.id.tue_tv);
        tue_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c.daysOfWeek.contains("tue")) {
                    c.daysOfWeek = c.daysOfWeek.replace("tue","");
                    tue_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                    tue_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                }
                else{
                    c.daysOfWeek += "tue";
                    tue_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    tue_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
            }
        });
        wed_tv = (TextView) findViewById(R.id.wed_tv);
        wed_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c.daysOfWeek.contains("wed")) {
                    c.daysOfWeek = c.daysOfWeek.replace("wed","");
                    wed_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                    wed_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                }
                else{
                    c.daysOfWeek += "wed";
                    wed_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    wed_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
            }
        });
        thu_tv = (TextView) findViewById(R.id.thu_tv);
        thu_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c.daysOfWeek.contains("thu")) {
                    c.daysOfWeek = c.daysOfWeek.replace("thu","");
                    thu_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                    thu_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                }
                else{
                    c.daysOfWeek += "thu";
                    thu_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    thu_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
            }
        });
        fri_tv = (TextView) findViewById(R.id.fri_tv);
        fri_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c.daysOfWeek.contains("fri")) {
                    c.daysOfWeek = c.daysOfWeek.replace("fri","");
                    fri_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                    fri_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                }
                else{
                    c.daysOfWeek += "fri";
                    fri_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    fri_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
            }
        });
        sat_tv = (TextView) findViewById(R.id.sat_tv);
        sat_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c.daysOfWeek.contains("sat")) {
                    c.daysOfWeek = c.daysOfWeek.replace("sat","");
                    sat_tv.setTextColor(getResources().getColor(R.color.greyDark2));
                    sat_tv.setPaintFlags(sun_tv.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
                }
                else{
                    c.daysOfWeek += "sat";
                    sat_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    sat_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
            }
        });

        foods_et = (EditText) findViewById(R.id.foods_et);
        cv = (CardView) findViewById(R.id.card_cv);

        Calendar cal = Calendar.getInstance();
        String day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
        String month = String.format("%02d", cal.get(Calendar.MONTH) + 1);
        String year = String.format("%04d", cal.get(Calendar.YEAR));
        String nowString = day + "/" + month + "/" + year;

        if(isAdd) {
            single_cb.setChecked(true);
            repeat_cv.setCardBackgroundColor(getResources().getColor(R.color.grey));
            sun_tv.setEnabled(false);
            mon_tv.setEnabled(false);
            tue_tv.setEnabled(false);
            wed_tv.setEnabled(false);
            thu_tv.setEnabled(false);
            fri_tv.setEnabled(false);
            sat_tv.setEnabled(false);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            Resources r = mContext.getResources();
            int px  = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    96,
                    r.getDisplayMetrics()
            );

            int px8  = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8,
                    r.getDisplayMetrics()
            );


            layoutParams.setMargins(px8, px8, px8, px);
            cv.setLayoutParams(layoutParams);

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            try {
                now = new Date(format.parse(nowString).getTime());
                data = now;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            date_et.setText(day + "/" + month + "/" +  year);
        }
        else{
            if(!c.daysOfWeek.equals("")){
                single_cv.setCardBackgroundColor(getResources().getColor(R.color.grey));
                date_et.setEnabled(false);

                repeat_cb.setChecked(true);

                if(c.daysOfWeek.contains("sun")){
                    sun_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    sun_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                if(c.daysOfWeek.contains("mon")) {
                    mon_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    mon_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                if(c.daysOfWeek.contains("tue")) {
                    tue_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    tue_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                if(c.daysOfWeek.contains("wed")) {
                    wed_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    wed_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                if(c.daysOfWeek.contains("thu")) {
                    thu_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    thu_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                if(c.daysOfWeek.contains("fri")) {
                    fri_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    fri_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
                if(c.daysOfWeek.contains("sat")) {
                    sat_tv.setTextColor(getResources().getColor(R.color.bordoMain));
                    sat_tv.setPaintFlags(sun_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
            }
            else{
                single_cb.setChecked(true);
                repeat_cv.setCardBackgroundColor(getResources().getColor(R.color.grey));
                sun_tv.setEnabled(false);
                mon_tv.setEnabled(false);
                tue_tv.setEnabled(false);
                wed_tv.setEnabled(false);
                thu_tv.setEnabled(false);
                fri_tv.setEnabled(false);
                sat_tv.setEnabled(false);
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            Resources r = mContext.getResources();
            int px  = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    168,
                    r.getDisplayMetrics()
            );

            int px8  = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8,
                    r.getDisplayMetrics()
            );

            layoutParams.setMargins(px8, px8, px8, px);
            cv.setLayoutParams(layoutParams);

            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            String dateString = df.format(c.date);
            try {
                now = new Date(df.parse(nowString).getTime());
                data = new Date(df.parse(dateString).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            date_et.setText(dateString);
            foods_et.setText(c.card);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (foods_et.getText().toString().isEmpty()) {
                    Toast.makeText(mContext, getString(R.string.card_empty), Toast.LENGTH_SHORT).show();
                }
                else if ((single_cb.isChecked())&&(data.getTime() < now.getTime())) {
                    Toast.makeText(mContext, getString(R.string.invalid_date), Toast.LENGTH_SHORT).show();
                }
                else if ((repeat_cb.isChecked())&&(c.daysOfWeek.equals(""))){
                    Toast.makeText(mContext, getString(R.string.invalid_days), Toast.LENGTH_SHORT).show();
                }
                else {
                    if(isOnline()){
                        if (isAdd) {
                            ProgressDialog();
                            if(single_cb.isChecked())
                                getCardByDate();
                            else
                                createCard();
                        } else {
                            ProgressDialog();
                            updateCard();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), getString(R.string.noconnection), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        FloatingActionButton fab_delete = (FloatingActionButton) findViewById(R.id.delete_fab);
        fab_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDeleteConfirmDialog();
            }
        });

        if(isAdd){
            fab_delete.setVisibility(View.GONE);
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

    public void getCardByDate(){
        final HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("restaurant", restaurantID);
        params1.put("date", String.valueOf(data.getTime() / 1000));
        Kumulos.call("getCardByDate", params1, new ResponseHandler() {
            @Override
            public void onFailure(@Nullable Throwable error) {
                getCardByDateError++;
                if(getCardByDateError >= 3){
                    getCardByDateError = 0;
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    getCardByDate();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                objects1 = (ArrayList<LinkedHashMap<String, Object>>) result;
                if (objects1.isEmpty()) {
                    createCard();
                } else {
                    updateCard2();
                }
            }
        });
    }

    public void createCard(){
        //create
        final HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("card", foods_et.getText().toString());
        params1.put("restaurant", restaurantID);
        if(single_cb.isChecked())
            params1.put("date", String.valueOf(data.getTime() / 1000));
        else
            params1.put("daysOfWeek", c.daysOfWeek);
        Kumulos.call("createCard", params1, new ResponseHandler() {
            @Override
            public void onFailure(@Nullable Throwable error) {
                createCardError++;
                if(createCardError >= 3){
                    createCardError = 0;
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    createCard();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                if((int)result > -1) {
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.card_created), Toast.LENGTH_SHORT).show();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        finish();
                    }
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.card_created_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateCard2(){
        String ID = String.valueOf(objects1.get(0).get("cardID"));
        final HashMap<String, String> params2 = new HashMap<String, String>();
        params2.put("card", foods_et.getText().toString());
        if(single_cb.isChecked())
            params2.put("date", String.valueOf(data.getTime() / 1000));
        else
            params2.put("daysOfWeek", c.daysOfWeek);
        params2.put("cardID", ID);
        Kumulos.call("updateCard", params2, new ResponseHandler() {
            @Override
            public void onFailure(@Nullable Throwable error) {
                updateCardError++;
                if(updateCardError >= 3){
                    updateCardError = 0;
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    updateCard2();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                if((int)result == 1) {
                    Toast.makeText(mContext, getString(R.string.card_updated), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        finish();
                    }
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.card_updated_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateCard(){
        final HashMap<String, String> params2 = new HashMap<String, String>();
        params2.put("card", foods_et.getText().toString());
        if(single_cb.isChecked())
            params2.put("date", String.valueOf(data.getTime() / 1000));
        else
            params2.put("daysOfWeek", c.daysOfWeek);
        params2.put("cardID", c.cardID);
        Kumulos.call("updateCard", params2, new ResponseHandler() {
            @Override
            public void onFailure(@Nullable Throwable error) {
                updateCardError++;
                if(updateCardError>= 3){
                    progressDialog.dismiss();
                    updateCardError = 0;
                    Toast.makeText(mContext, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    updateCard();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                if ((int) result == 1) {
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.card_updated), Toast.LENGTH_SHORT).show();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        finish();
                    }

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.card_updated_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void deleteCard(){
        //delete card
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("restaurant", restaurantID);
        params.put("cardID", c.cardID);
        Kumulos.call("removeCard", params, new ResponseHandler() {
            @Override
            public void onFailure(@Nullable Throwable error) {
                removeCardError++;
                if(removeCardError >= 3){
                    removeCardError = 0;
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.database_fail), Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    deleteCard();
                }
                super.onFailure(error);
            }

            @Override
            public void didCompleteWithResult(@Nullable Object result) {
                super.didCompleteWithResult(result);
                if((int)result == 1) {
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.removed_card), Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(mContext, getString(R.string.removed_card_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("89CC9100943D6BF03D38BBF9775C79FA")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }


    private void showDatePicker() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View dialogView = inflater.inflate(R.layout.calendar_layout, null);
        final CalendarView cv = (CalendarView) dialogView.findViewById(R.id.calendar_view);

        Calendar cal = Calendar.getInstance();

        long milis = cal.getTimeInMillis();
        cv.setDate(milis);

        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        cv.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String dataString = String.format("%02d", dayOfMonth) + "/" + String.format("%02d", month + 1) + "/" + String.format("%04d", year);
                date_et.setText(dataString);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    data = new Date(format.parse(dataString).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                alertDialog.dismiss();
                return;
            }
        });
        return;
    }

    AlertDialog dialog;
    public void ShowDeleteConfirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Tem certeza que deseja deletar o cardápio?")
                .setTitle("Deletar Cardápio");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                if(isOnline()) {
                    ProgressDialog();
                    deleteCard();
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
}
