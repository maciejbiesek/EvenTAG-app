package com.example.maciej.eventag.Helpers;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddressHelper {

    private Context context;

    public AddressHelper(Context context) {
        this.context = context;
    }

    public String getAddress(Double lat, Double lng) {
        Geocoder geocoder;
        List<Address> addresses = new ArrayList<Address>();
        geocoder = new Geocoder(this.context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
        }

        if (!addresses.isEmpty()) {
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            return address + ", " + city;
        }
        else {
            return lat + ", " + lng;
        }
    }
}
