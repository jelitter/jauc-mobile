package cit.jauc.model;

import java.util.List;

public class Ticket {

    private String ticketId;
    private String userId;
    private String ticketText;
    private List<String> additionalDetail;

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTicketText() {
        return ticketText;
    }

    public void setTicketText(String ticketText) {
        this.ticketText = ticketText;
    }

    public List<String> getAdditionalDetail() {
        return additionalDetail;
    }

    public void setAdditionalDetail(List<String> additionalDetail) {
        this.additionalDetail = additionalDetail;
    }
}
