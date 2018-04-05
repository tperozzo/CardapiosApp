package com.perozzo.cardapiosapp.ui;

import android.location.Address;

/**
 * Created by Perozzo on 08/04/2017.
 */

public class CitySearchResult {
    public Address address;
    public String addressString;

    public CitySearchResult(Address address)
    {
        this.address = address;
    }

    public String[] getCity(){

        String [] display_address = new String[2];
        display_address[0] = "";
        display_address[1] = "";

        if(address.getLocality() != null)
            display_address[0] += address.getLocality();  //cidade
        if(address.getAdminArea() != null)
            display_address[1] += address.getAdminArea() + ", ";  //estado
        if(address.getCountryName() != null)
            display_address[1] += address.getCountryName() ;  //país

        //cidade
        //estado, país
        return display_address;
    }

    public String toEditText(){
        String display_address = "";

        if(address.getLocality() != null) {
            display_address += address.getLocality();  //cidade
            addressString = address.getLocality();
        }

        if(address.getCountryName() != null)
            display_address += " - " + address.getCountryName(); //pais
        //cidade
        return display_address;
    }
}
