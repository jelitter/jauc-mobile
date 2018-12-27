package cit.jauc.model;

public class Car {
    private String name;
    private String plate;
    private Location location;

    public Car(String name, String plate, long locationLongitude, long locationLatitude) {
        this.name = name;
        this.plate = plate;
        this.location.setLon(locationLongitude);
        this.location.setLat(locationLatitude);
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
