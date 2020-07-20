package com.example.tovisit_prakash_c0773839_android;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

public class DataParser {
    private HashMap<String, String> getPlace(JSONObject jsonObject) {

        Log.i(TAG, "getPlace: ");
        HashMap<String, String> place = new HashMap<>();
        String placeName = "N/A";
        String vicinity = "N/A";
        String latitude = "";
        String longitude = "";
        String reference = "";

        try {
            if (!jsonObject.isNull("name")) {
                placeName = jsonObject.getString("name");
            }
            if (!jsonObject.isNull("vicinity")) {
                vicinity = jsonObject.getString("vicinity");
            }

            latitude = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = jsonObject.getString("reference");

            place.put("placeName", placeName);
            place.put("vicinity", vicinity);
            place.put("lat", latitude);
            place.put("lng", longitude);
            place.put("reference", reference);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return place;
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray){

        Log.i(TAG, "getPlaces: ");

        int count = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> place = null;

        for (int i = 0; i < count; i++){
            try {
                place = getPlace( (JSONObject) jsonArray.get(i));
                placesList.add(place);

            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        return placesList;

    }

    public List<HashMap<String, String>> parseData(String jsonData){


        Log.i(TAG, "parseData: ");
        JSONArray jsonArray = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");

        } catch (JSONException e){
            e.printStackTrace();
        }

        return getPlaces(jsonArray);
    }

    public HashMap<String, String> parseDistance(String jsonData){

        Log.i(TAG, "parseDistance: ");
        JSONArray jsonArray = null;
        try{
            JSONObject jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");

        } catch(JSONException e){


            e.printStackTrace();
            return new HashMap<String, String>();
        }

        return getDuration(jsonArray);

    }


    private HashMap<String, String> getDuration(JSONArray directionJSONData){

        Log.i(TAG, "getDuration: ");
        HashMap<String, String> directionMap = new HashMap<>();
        String duration = "", distance = "";

        try {
            duration = directionJSONData.getJSONObject(0).getJSONObject("duration").getString("text");
            distance = directionJSONData.getJSONObject(0).getJSONObject("distance").getString("text");

            directionMap.put("duration", duration);
            directionMap.put("distance", distance);
        } catch (JSONException e) {
            e.printStackTrace();
            return new HashMap<String, String>();
        }

        return directionMap;
    }

    public String[] parseDirections(String jsonData){

        Log.i(TAG, "parseDirections: ");
        JSONArray jsonArray = null;
        try{
            JSONObject jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes")
                    .getJSONObject(0)
                    .getJSONArray("legs")
                    .getJSONObject(0)
                    .getJSONArray("steps");

        } catch(JSONException e){
            e.printStackTrace();
            return new String[]{};
        }

        return getPaths(jsonArray);
    }

    private String[] getPaths(JSONArray jsonArray){

        Log.i(TAG, "getPaths: ");
        int count = jsonArray.length();
        String[] polylines = new String[count];

        for(int i = 0; i<count; i++){
            try{
                polylines[i] = getPath(jsonArray.getJSONObject(i));
            }catch (JSONException e){
                e.printStackTrace();
                return new String[]{};
            }
        }
        return polylines;
    }

    private String getPath(JSONObject jsonObject){
        Log.i(TAG, "getPath: ");
        String polyline = "";
        try {
            polyline = jsonObject.getJSONObject("polyline").getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return polyline;

    }
}
