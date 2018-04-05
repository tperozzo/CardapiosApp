package com.perozzo.cardapiosapp.ui;

import android.location.Address;

/**
 * Created by Perozzo on 08/04/2017.
 */

public class AddressSearchResult {
    public Address address;

    public AddressSearchResult(Address address)
    {
        this.address = address;
    }

    public String[] getAddress(){

        String [] display_address = new String[2];
        display_address[0] = "";
        display_address[1] = "";

        /*display_address += addr.getAddressLine(0) + "\n";

        for(int i = 1; i < addr.getMaxAddressLineIndex(); i++)
        {
            display_address += addr.getAddressLine(i) + ", ";
        }

        display_address = display_address.substring(0, display_address.length() - 2);*/
        if(address.getThoroughfare() != null)
            display_address[0] += address.getThoroughfare(); //rua
        if((address.getSubThoroughfare() != null)&&(address.getThoroughfare() != null))
            display_address[0] += ", ";
        if(address.getSubThoroughfare() != null)
            display_address[0] += address.getSubThoroughfare(); //numero

        if(address.getSubLocality() != null)
            display_address[1] += address.getSubLocality();  //bairro
        if((address.getSubLocality() != null)&&(address.getLocality() != null))
            display_address[1] += ", ";
        if(address.getLocality() != null)
            display_address[1] += address.getLocality();  //cidade

        return display_address;
    }

    public String toEditText(){
        String display_address = "";

        if(address.getThoroughfare() != null)
            display_address += address.getThoroughfare() ; //rua
        if((address.getSubThoroughfare() != null)&&(address.getThoroughfare() != null))
            display_address += ", ";
        if(address.getSubThoroughfare() != null)
            display_address += address.getSubThoroughfare(); //numero

        return display_address;
    }
}
