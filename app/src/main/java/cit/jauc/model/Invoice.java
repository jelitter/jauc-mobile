package cit.jauc.model;

import java.io.Serializable;

public class Invoice implements Serializable {

    private String id;
    private boolean isPaid;
    private int price;
    private String description;
    private StripeCustomer customer;

    public Invoice() {
    }

    public Invoice(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public boolean isPaid() {
        return isPaid;
    }
    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public double getPrice() {
        return price / 100;
    }
    public void setPrice(double price) {
        this.price = (int) Math.round(price * 100);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StripeCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(StripeCustomer customer) {
        this.customer = customer;
    }
}
