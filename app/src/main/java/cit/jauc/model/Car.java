package cit.jauc.model;

import java.io.Serializable;

public class Car implements Serializable {

    private String id;
    private String name;
    private String plate;
    private Location location;

    public Car() {
        this.location = new Location();
    }

    public Car(String name, String plate, long locationLongitude, long locationLatitude) {
        this.name = name;
        this.plate = plate;
        this.location = new Location();
        this.location.setLon(locationLongitude);
        this.location.setLat(locationLatitude);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }


}
