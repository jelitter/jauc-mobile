package cit.jauc.model;

import java.io.Serializable;
import java.util.Date;

public class Booking implements Serializable {
    private String carId;
    private Invoice invoice;
    private String userId;
    private Location origin;
    private Location destination;
    private Date bookingDate;
    private String id;
    private Car car;

    public Booking() {
        this.origin = new Location();
        this.destination = new Location();
        this.invoice = new Invoice();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Location getOrigin() {
        return origin;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public void setOrigin(double longitude, double latitude) {
        this.origin.setLon(longitude);
        this.origin.setLat(latitude);
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public void setDestination(double longitude, double latitude) {
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


    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

}
