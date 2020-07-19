package com.example.tovisit_prakash_c0773839_android;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.content.ContentValues.TAG;

public class DirectionDataFetcher extends AsyncTask<Object, String, String> {



    String directionData, url;
    GoogleMap mMap;
    LatLng latLng;

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
        Log.i(TAG, "doInBackground: GET DIRECTION DATA ");
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];
        latLng = (LatLng) objects[2];

        FetchURL fetchURL= new FetchURL();
        try{
            directionData = fetchURL.readURL(url);
        } catch (IOException e){
            e.printStackTrace();
        }

        return directionData;
    }
}
