package com.example.tovisit_prakash_c0773839_android;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tovisit_prakash_c0773839_android.database.LocalCacheManager;
import com.example.tovisit_prakash_c0773839_android.database.Place;
import com.example.tovisit_prakash_c0773839_android.interfaces.AddPlaceViewInterface;
import com.example.tovisit_prakash_c0773839_android.interfaces.EditPlaceViewInterface;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback, EditPlaceViewInterface, AddPlaceViewInterface {
    private GoogleMap gMap;


    private static final String TAG = "MAP_VIEW";
    private static final int ZOOM = 13;

     View v;

    private static final int RADIUS = 1700;
    private static final int REQUEST_CODE = 100;

    private static final long WAIT_TIME = 5L;
    private boolean isVisited = false;
    CheckBox checkBox;
    RadioGroup mapTypeRadioGroup;


    LocationManager locationManager;
    LocationListener locationListener;
    Geocoder geocoder;

    Location currentLocation;
    Marker startLocation, current, favPlace;
    Boolean editMode = false;
    AlertDialog markerMenu;

    Place currentPlaceObj;

    private String placeName;
    private Object[] dataTransferObj;

    String strResult = null;
    Place place = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);



        Intent intent = getIntent();
        Place place = (Place) intent.getSerializableExtra("placeObj");
        if(place != null){
            setTitle(place.getPlaceName());
        }


        mapTypeRadioGroup = findViewById(R.id.map_type_radio);
        mapTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.standard:
                        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;

                    case R.id.satellite:
                        gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;

                    case R.id.terrain:
                        gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;

                    case R.id.hybrid:
                        gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                }



            }
        });
        currentPlaceObj = (Place) intent.getSerializableExtra("placeObj");

        // setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setUpLocationObjs();

    }



    private void setUpLocationObjs(){
        geocoder = new Geocoder(this, Locale.getDefault());
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                if (location != null)
                {
                    currentLocation = location;
                    userLocationMarker(currentLocation);
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };
    }


    private void UpadteCamera(Location location){
        place = null;
        userLocationMarker(location);

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(ZOOM)
                .bearing(0)
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
                .icon(bitmapDescriptorFromVector(this, R.drawable.location))

        );
        current = startLocation;
    }


    private String getDirectionUrl()
    {
        if(startLocation == null)
        {
            Log.d(TAG, "getDirectionUrl: startLocation is null");
        }
        return "https://maps.googleapis.com/maps/api/directions/json?" + "origin=" + startLocation.getPosition().latitude + "," + startLocation.getPosition().longitude +
                "&destination=" + favPlace.getPosition().latitude + "," + favPlace.getPosition().longitude +
                "&key=" + getString(R.string.google_map_api_key);
    }

    @SuppressLint("MissingPermission")
    public void setUp()
    {


        gMap.clear();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        userLocationMarker(currentLocation);

        Intent i = getIntent();
        place = (Place) i.getSerializableExtra("placeObj");

        if (place != null) {

            editMode = true;
            LatLng pos = new LatLng(place.getLatCord(), place.getLngCord());
            favPlace = gMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pin))
                    .title(place.getPlaceName()))
            ;

            favPlace.setDraggable(true);



            CameraPosition cameraPosition = CameraPosition.builder()
                    .target(pos)
                    .zoom(ZOOM)
                    .bearing(0)
                    .tilt(0)
                    .build();
            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    favPlace = marker;
                    dataTransferObj = new Object[3];
                    dataTransferObj[0] = gMap;
                    dataTransferObj[1] = getDirectionUrl();
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
                    distanceHashMap = distanceParser.parseDistance(strResult);
                    showMarkerClickedAlert(marker.getTitle(), distanceHashMap.get("distance"), distanceHashMap.get("duration"));
                    return true;
                }
            });

            gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) { }

                @Override
                public void onMarkerDrag(Marker marker) { }

                @Override
                public void onMarkerDragEnd(Marker marker)
                {

                    Intent intent = getIntent();
                    String  newAddress = getAddress(marker);
                    Place oldPlace = (Place) intent.getSerializableExtra("placeObj");

                    favPlace = marker;
                    if(oldPlace != null){
                        Place place = new Place(marker.getPosition().latitude, marker.getPosition().longitude,
                                newAddress, isVisited);
                        place.setId(oldPlace.getId());
                        LocalCacheManager.getInstance(DetailActivity.this).updatePlace(DetailActivity.this,place);


                    }
                }
            });
        } else {

            UpadteCamera(currentLocation);
        }

    }


    public void saveFavorite()
    {
        placeName = favPlace.getTitle();

        CheckBox checkBox = v.findViewById(R.id.visitedCheckBox);


        Place placeUpdated = new Place(
                favPlace.getPosition().latitude, favPlace.getPosition().longitude,placeName, checkBox.isChecked()
        );
        placeUpdated.setId(currentPlaceObj.getId());
        LocalCacheManager.getInstance(this).updatePlace(this,placeUpdated);

    }

    public void onMarkerClick(View view) {
        switch (view.getId())
        {
            case R.id.addToFvtBtn:
                saveFavorite();
                break;
            case R.id.getDirBtn:

                gMap.clear();
                current = null;

                String[] directionsList;
                DataParser directionParser = new DataParser();
                directionsList = directionParser.parseDirections(strResult);

                for (String s : directionsList) {
                    PolylineOptions options = new PolylineOptions()
                            .color(Color.BLACK)
                            .width(15)
                            .addAll(PolyUtil.decode(s));
                    gMap.addPolyline(options);
                }

                startLocation = gMap.addMarker(new MarkerOptions().position(startLocation.getPosition())
                        .title(startLocation.getTitle())
                        .icon(bitmapDescriptorFromVector(this, R.drawable.location))
                );
                favPlace = gMap.addMarker(new MarkerOptions().position(favPlace.getPosition())
                        .title(favPlace.getTitle())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pin))
                );
                favPlace.showInfoWindow();
                break;
            default:
                break;
        }
        markerMenu.dismiss();
    }


    public String getAddress(Marker m)
    {
        try
        {
            int addressIndex =0;
            int addressLine = 0;
            List<Address> addressList = geocoder.getFromLocation(m.getPosition().latitude, m.getPosition().longitude,1);
            return addressList.get(addressIndex).getAddressLine(addressLine);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        String format = simpleDateFormat.format(new Date());
        return format;
    }



    private void showMarkerClickedAlert(String address, String distance, String duration)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(DetailActivity.this);
        v = LayoutInflater.from(DetailActivity.this).inflate(R.layout.marker_data_menu, null);
        alert.setView(v);

        TextView tvPlaceName = v.findViewById(R.id.place_name);
        TextView tvDistance = v.findViewById(R.id.distance);
        TextView tvDuration = v.findViewById(R.id.duration);
        Button saveBtn = v.findViewById(R.id.addToFvtBtn);
        CheckBox checkBox = v.findViewById(R.id.visitedCheckBox);

        Intent intent = getIntent();
        Place place = (Place) intent.getSerializableExtra("placeObj");
        if(place != null){
            if(place.getVisited()){
                checkBox.setChecked(true);
            }
        }
        saveBtn.setText("Update");

        tvPlaceName.setText(address);
        tvDistance.setText("Distance: " + distance );
        tvDuration.setText("Duration: "+duration);


        markerMenu = alert.create();
        markerMenu.show();
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                setUp();
            }
            else
            {
                Toast.makeText(this, "Permission is required to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.getUiSettings().setZoomControlsEnabled(true);

        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (!(permissionState == PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
        else
        {
            setUp();
        }
    }

    @Override
    public void onPlaceUpdated() {
        Toast.makeText(DetailActivity.this, "Updated", Toast.LENGTH_SHORT).show();

    }

    private String getFetchUrl(double lat, double lng, String nearByPlace)
    {
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + "location=" + lat + "," + lng +
                "&radius=" + RADIUS +
                "&type=" + nearByPlace +
                "&key=" + getString(R.string.google_map_api_key);
    }

    @Override
    public void onPlaceAdded() {

        Toast.makeText(this,"Place added",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataNotAvailable() {

    }




}