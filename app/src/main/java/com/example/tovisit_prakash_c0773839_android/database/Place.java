package com.example.tovisit_prakash_c0773839_android.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Place implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private Double latCord, lngCord;
    private String placeName;
    private Boolean isVisited;

    public Place(int id, Double latCord, Double lngCord, String placeName, Boolean isVisited) {
        this.id = id;
        this.latCord = latCord;
        this.lngCord = lngCord;
        this.placeName = placeName;
        this.isVisited = isVisited;
    }

    public Place(){

    }

    public Place( Double latCord, Double lngCord, String placeName, Boolean isVisited) {
        this.latCord = latCord;
        this.lngCord = lngCord;
        this.placeName = placeName;
        this.isVisited = isVisited;
    }

    public int getId() {
        return id;
    }

    public Double getLatCord() {
        return latCord;
    }

    public Double getLngCord() {
        return lngCord;
    }

    public String getPlaceName() {
        return placeName;
    }

    public Boolean getVisited() {
        return isVisited;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLatCord(Double latCord) {
        this.latCord = latCord;
    }

    public void setLngCord(Double lngCord) {
        this.lngCord = lngCord;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public void setVisited(Boolean visited) {
        isVisited = visited;
    }
}
