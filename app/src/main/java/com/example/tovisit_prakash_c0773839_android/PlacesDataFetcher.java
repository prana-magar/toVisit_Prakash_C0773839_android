package com.example.tovisit_prakash_c0773839_android;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

public class PlacesDataFetcher extends AsyncTask<Object, String, String> {

    GoogleMap googleMap;
    String placeData, url;


    public class FetchURL {
        public String readURL(String myUrl) throws IOException {

            Log.i(TAG, "fetchUrl: ");

            String data = "";
            InputStream inputStream = null;

            HttpURLConnection httpURLConnection = null;

            try {
                URL url = new URL(myUrl);
                httpURLConnection = (HttpURLConnection) url.openConnection();

                inputStream = httpURLConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer stringBuffer = new StringBuffer();

                String line = "";
                while((line = reader.readLine()) != null)
                    stringBuffer.append(line);

                data = stringBuffer.toString();
                reader.close();


            } catch(MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                inputStream.close();
                httpURLConnection.disconnect();
            }

            return data;
        }
    }

    @Override
    protected String doInBackground(Object... objects) {


        googleMap = (GoogleMap) objects[0];
        url = (String) objects[1];

        FetchURL fetchURL= new FetchURL();
        try{
            placeData = fetchURL.readURL(url);
        } catch (IOException e){
            e.printStackTrace();
        }

        return placeData;
    }
    @Override
    protected void onPostExecute(String s) {

        List<HashMap<String, String>> nearByPlaceList = null;
        DataParser parser = new DataParser();
        nearByPlaceList = parser.parseData(s);
        showNearbyPlaces(nearByPlaceList);
    }


    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList){

        for(int i=0; i<nearbyPlacesList.size(); i++){
            HashMap<String, String> place = nearbyPlacesList.get(i);

            String placeName = place.get("placeName");
            String vicinity = place.get("vicinity");
            double latitude = Double.parseDouble(place.get("lat"));
            double longitude = Double.parseDouble(place.get("lng"));
            String reference = place.get("reference");

            LatLng latLng = new LatLng(latitude, longitude);

            //marker options
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(placeName + "\n" + vicinity)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.home_run));
            googleMap.addMarker(markerOptions);

        }
    }
}
