package cit.jauc.model;

public class Location {
    private long lon;
    private long lat;

    public Location() {

    }

    public Location(long lon, long lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public long getLon() {
        return lon;
    }

    public void setLon(long lon) {
        this.lon = lon;
    }

    public long getLat() {
        return lat;
    }

    public void setLat(long lat) {
        this.lat = lat;
    }


}
