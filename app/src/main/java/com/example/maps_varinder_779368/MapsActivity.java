package com.example.maps_varinder_779368;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap googleMap;
    private double latitude;
    private double longitude;
    private PolygonOptions polygonOptions = new PolygonOptions();
    private Gson gson = new Gson();
    private SessionData sessionData = new SessionData();
    private SharedPreferences.Editor editor;
    private int changeMarkerPosition = -2;
    private boolean isNotChanged = true;
    private float distance = 0;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        initViews();

    }

    private void initViews() {
        SharedPreferences sharedpreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        String data = sharedpreferences.getString("data", "");
        assert data != null;
        if (!data.isEmpty()) {
            sessionData = (gson.fromJson(data, SessionData.class));
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.clear();
        showAllPins();

        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                latitude = latLng.latitude;
                longitude = latLng.longitude;

                if (sessionData.locationList.size() < 4) {
                    MapsActivity.this.googleMap.clear();
                    LatLngCoordinates.getAddressFromLocation(latLng.latitude, latLng.longitude,
                            getApplicationContext(), new GeoCoderHandler());
                    if (!sessionData.locationList.isEmpty()) {
                        showAllPins();
                    }
                } else {
                    for (int i = 0; i < sessionData.locationList.size(); i++) {
                        double latitude = sessionData.locationList.get(i).getLatitude();
                        double longitude = sessionData.locationList.get(i).getLongitude();
                        Location locationStart = new Location("locationStart");
                        locationStart.setLatitude(latitude);
                        locationStart.setLongitude(longitude);
                        double latitude1 = 0;
                        double longitude1 = 0;
                        if (i == sessionData.locationList.size() - 1) {
                            latitude1 = sessionData.locationList.get(0).getLatitude();
                            longitude1 = sessionData.locationList.get(0).getLongitude();
                        } else {
                            latitude1 = sessionData.locationList.get(i + 1).getLatitude();
                            longitude1 = sessionData.locationList.get(i + 1).getLongitude();
                        }
                        Location locationEnd = new Location("locationEnd");
                        locationEnd.setLatitude(latitude1);
                        locationEnd.setLongitude(longitude1);
                        distance += locationStart.distanceTo(locationEnd) / 1000;

                    }

                    Toast.makeText(MapsActivity.this, "Distance of all pin is: " + distance, Toast.LENGTH_SHORT).show();

                }
            }
        });

        this.googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                MapsActivity.this.googleMap.clear();
                isNotChanged = false;
                for (int i = 0; i < sessionData.locationList.size(); i++) {
                    if (marker.getTitle().equalsIgnoreCase(sessionData.locationList.get(i).getPlaceName())) {
                        changeMarkerPosition = i;
                    }
                }
                LatLng latLng = marker.getPosition();
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                LatLngCoordinates.getAddressFromLocation(latLng.latitude, latLng.longitude,
                        MapsActivity.this, new GeoCoderHandler());
            }
        });
        this.googleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                double latitudeA = polyline.getPoints().get(0).latitude;
                double longitudeA = polyline.getPoints().get(0).longitude;
                double latitudeB = polyline.getPoints().get(1).latitude;
                double longitudeB = polyline.getPoints().get(1).longitude;

                float distance = 0;
                Location currentLocation = new Location("currentLocation");
                currentLocation.setLatitude(latitudeA);
                currentLocation.setLongitude(longitudeA);

                Location newLocation = new Location("newLocation");
                newLocation.setLatitude(latitudeB);
                newLocation.setLongitude(longitudeB);


                distance = currentLocation.distanceTo(newLocation) / 1000; // in km
                Toast.makeText(MapsActivity.this, "" + distance, Toast.LENGTH_SHORT).show();

            }
        });

        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (!sessionData.locationList.isEmpty()) {
                    sessionData.locationList.remove(sessionData.locationList.size() - 1);
                    saveSessionData();
                    googleMap.clear();
                    showAllPins();
                    Toast.makeText(MapsActivity.this, " Deleted Succesfully",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showAllPins() {
        ArrayList<LatLng> locationarray = new ArrayList<>();
        if (!sessionData.locationList.isEmpty()) {
            for (int i = 0; i < sessionData.locationList.size(); i++) {
                MapsData data = sessionData.locationList.get(i);
                LatLng places = new LatLng(data.getLatitude(), data.getLongitude());
                locationarray.add(places);
                googleMap.addMarker(new MarkerOptions().position(places).title(data.getPlaceName()).draggable(true));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(places, 9));
            }

            addPolyLines(locationarray);
            boundaryColor(locationarray);
        }
    }

    private void addPolyLines(ArrayList<LatLng> location) {
        googleMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .addAll(location)
                .color(Color.RED)
                .add(location.get(0)));
    }

    private void boundaryColor(ArrayList<LatLng> location) {
        polygonOptions.addAll(location);
        if (polygonOptions.getPoints().size() > 3) {
            polygonOptions.strokeWidth((float) 0.30);
            polygonOptions.fillColor(getResources().getColor(R.color.colorTransparentGreen));
            Polygon polygon = googleMap.addPolygon(polygonOptions);
        }

    }

    private void saveSessionData() {
        String data = gson.toJson(sessionData, SessionData.class);
        editor.putString("data", data);
        editor.apply();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Info window clicked",
                Toast.LENGTH_SHORT).show();
    }

    private class GeoCoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            if (!isNotChanged) {
                isNotChanged = true;
                MapsData mapsData = new MapsData();
                mapsData.setLongitude(longitude);
                mapsData.setLatitude(latitude);
                mapsData.setPlaceName(locationAddress);
                sessionData.locationList.set(changeMarkerPosition, mapsData);
                ArrayList<LatLng> places = new ArrayList<>();
                for (int i = 0; i < sessionData.locationList.size(); i++) {
                    LatLng latlng = new LatLng(sessionData.locationList.get(i).getLatitude(), sessionData.locationList.get(i).getLongitude());
                    places.add(latlng);
                }
                boundaryColor(places);
            } else {
                MapsData mapsData = new MapsData();
                mapsData.setLongitude(longitude);
                mapsData.setLatitude(latitude);
                mapsData.setPlaceName(locationAddress);
                sessionData.locationList.add(mapsData);
            }
            saveSessionData();
            showAllPins();

        }
    }

    public class SessionData {
        public ArrayList<MapsData> locationList = new ArrayList<>();
    }
}

