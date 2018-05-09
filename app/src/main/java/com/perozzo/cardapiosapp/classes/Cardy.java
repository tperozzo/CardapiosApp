package com.perozzo.cardapiosapp.classes;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Perozzo on 14/03/2017.
 */

public class Cardy implements Serializable{

    @SerializedName("date")
    public Date date;

    @SerializedName("cardID")
    public String cardID;

    @SerializedName("card")
    public String card;

    @SerializedName("daysOfWeek")
    public String daysOfWeek = "";
}
