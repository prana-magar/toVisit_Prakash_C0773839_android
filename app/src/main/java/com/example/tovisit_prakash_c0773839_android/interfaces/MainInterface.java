package com.example.tovisit_prakash_c0773839_android.interfaces;

import com.example.tovisit_prakash_c0773839_android.database.Place;

import java.util.List;

public interface MainInterface {

    void onPlacesLoaded(List<Place> places);

    void onPlaceAdded();

    void onDataNotAvailable();
}
