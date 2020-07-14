package com.example.maps_varinder_779368;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LatLngCoordinates {
    private static final String TAG = "LatLngCoordinates";

    public static void getAddressFromLocation(final double latitude, final double longitude,
                                              final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String data = null;
                try {
                    List<Address> addressList = geocoder.getFromLocation(
                            latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            stringBuilder.append(address.getAddressLine(i)).append("\n");
                        }
                        stringBuilder.append(address.getLocality()).append("\n");
                        stringBuilder.append(address.getCountryName());
                        data = stringBuilder.toString();
                    }
                } catch (IOException e) {

                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (data != null) {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("address", data);
                        message.setData(bundle);
                    } else {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        data = "Latitude: " + latitude + " Longitude: " + longitude +
                                "\n Nothing to show.";
                        bundle.putString("address", data);
                        message.setData(bundle);
                    }
                    message.sendToTarget();
                }
            }
        };
        thread.start();
    }
}
