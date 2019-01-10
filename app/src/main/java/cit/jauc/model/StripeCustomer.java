package cit.jauc.model;

import java.io.Serializable;

public class StripeCustomer extends User implements Serializable {

    private String customerToken;
    private String last4;

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

    public String getLast4() {
        return last4;
    }

    public void setLast4(String last4) {
        this.last4 = last4;
    }

}