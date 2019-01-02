package cit.jauc.model;

import java.io.Serializable;

public class Location implements Serializable {
    private double lon;
    private double lat;

    public Location() {

    }

    public Location(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }


}
