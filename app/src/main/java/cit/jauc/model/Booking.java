package cit.jauc.model;

public class Booking {
    private String carId;
    private Invoice invoice;
    private String userId;
    private float originLon;
    private float originLat;
    private float destinationLon;
    private float destinationLat;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getOriginLon() {
        return originLon;
    }

    public void setOriginLon(float originLon) {
        this.originLon = originLon;
    }

    public float getOriginLat() {
        return originLat;
    }

    public void setOriginLat(float originLat) {
        this.originLat = originLat;
    }

    public float getDestinationLon() {
        return destinationLon;
    }

    public void setDestinationLon(float destinationLon) {
        this.destinationLon = destinationLon;
    }

    public float getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(float destinationLat) {
        this.destinationLat = destinationLat;
    }
}
