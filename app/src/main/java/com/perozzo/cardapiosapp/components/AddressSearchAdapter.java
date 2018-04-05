package com.perozzo.cardapiosapp.components;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.perozzo.cardapiosapp.R;
import com.perozzo.cardapiosapp.ui.AddressSearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Perozzo on 10/04/2017.
 */

public class AddressSearchAdapter extends BaseAdapter implements Filterable {

    public String mInit = "";
    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List<AddressSearchResult> resultList = new ArrayList();

    public AddressSearchAdapter(Context context, String init) {
        mContext = context;
        mInit = init;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public AddressSearchResult getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.address_item, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.address_tv)).setText(getItem(position).getAddress()[0]);
        ((TextView) convertView.findViewById(R.id.bairro_city_tv)).setText(getItem(position).getAddress()[1]);
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List locations = findLocations(mContext, constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = locations;
                    filterResults.count = locations.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    private List<AddressSearchResult> findLocations(Context context, String query_text) {

        List<AddressSearchResult> geo_search_results = new ArrayList<AddressSearchResult>();

        Geocoder geocoder = new Geocoder(context, context.getResources().getConfiguration().locale);
        List<Address> addresses = null;

        try {
            // Getting a maximum of 15 Address that matches the input text
            addresses = geocoder.getFromLocationName(query_text + " " + mInit, 5);

            for(int i=0;i<addresses.size();i++){
                Address address = (Address) addresses.get(i);
                if(address.getMaxAddressLineIndex() != -1)
                {
                    geo_search_results.add(new AddressSearchResult(address));
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return geo_search_results;
    }
}