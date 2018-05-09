package com.perozzo.cardapiosapp.classes;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Perozzo on 14/03/2017.
 */

public class Restaurant implements Serializable {

    @SerializedName("icon")
    public String icon;

    @SerializedName("restaurantID")
    public String restaurantID;

    @SerializedName("name")
    public String name;

    @SerializedName("addr")
    public String addr;

    @SerializedName("fullAddr")
    public String fullAddr;

    @SerializedName("city")
    public String city;

    @SerializedName("compl")
    public String compl;

    @SerializedName("owner")
    public String owner;

    @SerializedName("byCoords")
    public String byCoords;

    @SerializedName("openTime")
    public String openTime;

    @SerializedName("closeTime")
    public String closeTime;

    @SerializedName("telephone")
    public String telephone;

    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;
}
