package com.example.tovisit_prakash_c0773839_android;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.tovisit_prakash_c0773839_android.database.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap gMap;


    private static final String TAG = "MAP_VIEW";



    private static final int RADIUS = 1700;
    private static final int REQUEST_CODE = 100;

    private static final long WAIT_TIME = 5L;

    LocationManager locationManager;
    LocationListener locationListener;
    Geocoder geocoder;

    Location currentLocation;
    Marker startLocation, current, favPlace;
    Boolean isEditing = false;
    AlertDialog markerMenu;


   Button currentLocationBtn ;

    Spinner  typeMenu, nearbyMenu;

    private String placeName;
    private Object[] dataTransferObj;

    String strResult = null;
    Place place = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);



        nearbyMenu = findViewById(R.id.nearByPlaces);
        nearbyMenu.setSelection(0);
        typeMenu = findViewById(R.id.mapType);
        typeMenu.setSelection(1);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        currentLocationBtn = findViewById(R.id.currentLocationBtn);
        currentLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraUpdater(currentLocation);
            }
        });
    }


    private void CameraUpdater(Location location){
        place = null;
        userLocationMarker(location);

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(15)
                .bearing(0)
                .tilt(45)
                .build();
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId)
    {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, 100, 100);
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void userLocationMarker(Location l)
    {
        if(current != null)
        {
            current.remove();
            current = null;
        }

        LatLng home = new LatLng(l.getLatitude(), l.getLongitude());
        startLocation = gMap.addMarker(new MarkerOptions()
                .position(home)
                .title("Current Location")
                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_baseline_person_pin_circle_24))

        );
        current = startLocation;
    }


    private String getDirectionUrl()
    {
        if(startLocation == null)
        {
            Log.d(TAG, "getDirectionUrl: startLocation is null");
        }
        StringBuilder directionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        directionUrl.append("origin=" + startLocation.getPosition().latitude + "," + startLocation.getPosition().longitude);
        directionUrl.append("&destination=" + favPlace.getPosition().latitude + "," + favPlace.getPosition().longitude);
        directionUrl.append("&key=" + getString(R.string.google_map_api_key));
        return directionUrl.toString();
    }

    @SuppressLint("MissingPermission")
    public void setUpActivity()
    {


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        userLocationMarker(currentLocation);

        Intent i = getIntent();
        isEditing = i.getBooleanExtra("EDIT", false);
        Log.i(TAG, "isEditing: " + isEditing);
        place = (Place) i.getSerializableExtra("selectedPlace");

        if (place != null) {

            Log.i(TAG, "onMapReady: Place is not null good job");
            LatLng pos = new LatLng(place.getLatCord(), place.getLngCord());
            favPlace = gMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_favorite_24))
                    .title(isEditing ? "Drag for change" : place.getPlaceName()).draggable(isEditing));

            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(pos)
                    .zoom(15)
                    .bearing(0)
                    .tilt(45)
                    .build();
            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {

            CameraUpdater(currentLocation);
        }

        if (!isEditing)
        {
            if (favPlace != null) {
                favPlace.showInfoWindow();
            }

            gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    favPlace = marker;
                    dataTransferObj = new Object[3];
                    dataTransferObj[0] = gMap;
                    dataTransferObj[1] = getDirectionUrl();
                    Log.i(TAG, "directionURL: " + getDirectionUrl());
                    dataTransferObj[2] = favPlace.getPosition();

                    DirectionDataFetcher getDirectionData = new DirectionDataFetcher();
                    // async
                    getDirectionData.execute(dataTransferObj);

                    try
                    {
                        strResult = getDirectionData.get(WAIT_TIME, TimeUnit.SECONDS);

                    }
                    catch (ExecutionException e)
                    {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }

                    HashMap<String, String> distanceHashMap = null;
                    DataParser distanceParser = new DataParser();
                    distanceHashMap = distanceParser.parseDistance(s);
                    showMarkerClickedAlert(marker.getTitle(), distanceHashMap.get("distance"), distanceHashMap.get("duration"));
                    return true;
                }
            });

            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
            {
                @Override
                //Add new marker on long click
                public void onMapLongClick(LatLng latLng)
                {
                    MarkerOptions options = new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                    favoritePlace = mMap.addMarker(options);
                    favoritePlace.setTitle(getAddress(favoritePlace));
                    favoritePlace.showInfoWindow();
                }
            });

        }
        else
        {
            // When the user edits a location
            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) { }

                @Override
                public void onMarkerDrag(Marker marker) { }

                @Override
                public void onMarkerDragEnd(Marker marker)
                {
                    favoritePlace = marker;
                }
            });

            //Unhiding the elements that allow user to update a location and set it as visited
            findViewById(R.id.editModeLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.nearByPlaces).setVisibility(View.INVISIBLE);
            final CheckBox visited = findViewById(R.id.visitedCheckBox);
            visited.setChecked(mPlace.getVisited());
            // update button
            findViewById(R.id.updateBTN).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String newPlaceName = getAddress(favoritePlace);
                    Boolean success = mDatabase.updatePlace(mPlace.getId(), newPlaceName, visited.isChecked(),
                            favoritePlace.getPosition().latitude, favoritePlace.getPosition().longitude);
                    favoritePlace.setTitle(newPlaceName);
                    favoritePlace.showInfoWindow();
                    Toast.makeText(MapsActivity.this, success ? "Updated" : "Update failed", Toast.LENGTH_SHORT).show();
                    Intent mIntent = new Intent(MapsActivity.this, FavoritesActivity.class);
                    startActivity(mIntent);
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                setUpActivity();
            }
            else
            {
                Toast.makeText(this, "Permission is required to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}