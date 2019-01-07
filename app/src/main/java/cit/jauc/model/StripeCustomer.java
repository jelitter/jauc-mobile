package cit.jauc.model;

import java.io.Serializable;

public class StripeCustomer extends User implements Serializable {

    private String customerToken;

    public StripeCustomer() {
        super();
    }

    public StripeCustomer(String token) {
        super();
        this.customerToken = token;
    }

    public String getCustomerToken() {
        return customerToken;
    }

    public void setCustomerToken(String customerToken) {
        this.customerToken = customerToken;
    }

}