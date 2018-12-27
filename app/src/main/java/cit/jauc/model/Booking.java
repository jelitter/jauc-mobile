package cit.jauc.model;

public class Booking {
    private String carId;
    private Invoice invoice;
    private String userId;
    private Location origin;
    private Location destination;

    public Booking() {
        this.origin = new Location();
        this.destination = new Location();
        this.invoice = new Invoice();
    }

    public Location getOrigin() {
        return origin;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public void setOrigin(long longitude, long latitude) {
        this.origin.setLon(longitude);
        this.origin.setLat(latitude);
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public void setDestination(long longitude, long latitude) {
        this.destination.setLon(longitude);
        this.destination.setLat(latitude);
    }



    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public void setInvoice(String id) {
        this.invoice.setId(id);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
