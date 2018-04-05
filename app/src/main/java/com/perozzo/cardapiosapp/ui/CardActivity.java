package com.perozzo.cardapiosapp.ui;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.perozzo.cardapiosapp.R;

import java.util.List;

public class CardActivity extends AppCompatActivity {
    public String restaurantName;
    public String card;
    public String address, latitude, longitude, telephone, horario;

    public TextView card_tv;
    public LinearLayout telephoneLayout;
    public TextView address_tv;
    public TextView telephone_tv;
    public TextView horario_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardy);
        getWindow().setBackgroundDrawable(null);

        card_tv = (TextView) findViewById(R.id.card_tv);
        address_tv = (TextView) findViewById(R.id.address_tv);
        telephone_tv = (TextView) findViewById(R.id.telephone_tv);
        horario_tv = (TextView) findViewById(R.id.horario_tv);
        telephoneLayout = (LinearLayout) findViewById(R.id.telephone_layout);

        restaurantName = getIntent().getStringExtra("REST");
        card = getIntent().getStringExtra("CARD");
        address = getIntent().getStringExtra("ADDRESS");
        latitude = getIntent().getStringExtra("LATITUDE");
        longitude = getIntent().getStringExtra("LONGITUDE");
        horario = getIntent().getStringExtra("OPENTIME") + " " + getString(R.string.until) + " " + getIntent().getStringExtra("CLOSETIME");
        telephone = getIntent().getStringExtra("TELEPHONE");

        card_tv.setText(card);
        address_tv.setText(address);
        address_tv.setTextColor(getResources().getColor(R.color.bordoMain));
        address_tv.setPaintFlags(address_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        address_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("geo:" + latitude
                                    + "," + longitude
                                    + "?q=" + latitude
                                    + "," + longitude
                                    + "(" + restaurantName+ ")"));
                    intent.setComponent(new ComponentName(
                            "com.google.android.apps.maps",
                            "com.google.android.maps.MapsActivity"));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {

                    try {
                        startActivity(new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.google.android.apps.maps")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.apps.maps")));
                    }

                    e.printStackTrace();
                }
            }
        });
        horario_tv.setText(horario);
        if(telephone.length()==0)
            telephoneLayout.setVisibility(View.GONE);
        else
            telephone_tv.setText(telephone);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(restaurantName);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        FloatingActionButton whats_fab = (FloatingActionButton) findViewById(R.id.whats_fab);
        whats_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareWhatsApp();
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void ShareWhatsApp() {

        if(isOnline()) {
            try {
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");
                String text = "O cardápio do restaurante *" + restaurantName + "* é:\n" + parseCardToWhatsApp() + "\n" +
                        "Horário de atendimento: *" + horario + "*. Enviado via Meu Rango App (LINK GOOGLE PLAY)";
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, text);

                startActivity(whatsappIntent);


            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(this, "WhatsApp não está instalado", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(), getString(R.string.noconnection), Toast.LENGTH_SHORT).show();
        }

    }

    public String parseCardToWhatsApp(){
        String temp = "*- " + card + "*";
        String ret = temp.replaceAll("(\\r|\\n|\\r\\n)+", "*\n*- ");
        ret = "\n" + ret + "\n";
        return ret;
    }
}
