package model;

import java.io.Serializable;

public class Event implements Serializable {
    private static final long serialVersionUID = 1L;
    private int eventId; // DB: event_id
    private String title; // DB: title
    private String day; // DB: day
    private String venue; // DB: location
    private double price; // Not in DB, but kept for UI
    private int sold; // Not in DB, but kept for UI
    private int total; // DB: total_seats
    private boolean enabled; // DB: enabled

    // Remove old/incorrect constructors and add correct ones for DB mapping
    public Event(String title, String day, String venue, int total, int available, boolean enabled) {
        this(0, title, day, venue, total, available, enabled);
    }
    public Event(int eventId, String title, String day, String venue, int total, int available, boolean enabled) {
        this.eventId = eventId;
        this.title = title;
        this.day = day;
        this.venue = venue;
        this.total = total;
        this.sold = total - available;
        this.enabled = enabled;
        this.price = 0.0; // default, not in DB
    }
    // Add constructor with price for admin add/modify
    public Event(String title, String day, String venue, double price, int total, int available, boolean enabled) {
        this(0, title, day, venue, price, total, available, enabled);
    }
    public Event(int eventId, String title, String day, String venue, double price, int total, int available, boolean enabled) {
        this.eventId = eventId;
        this.title = title;
        this.day = day;
        this.venue = venue;
        this.price = price;
        this.total = total;
        this.sold = total - available;
        this.enabled = enabled;
    }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getTitle() { return title; }
    public String getDay() { return day; }
    public String getVenue() { return venue; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getAvailableSeats() { return total - sold; }
    public void setAvailableSeats(int available) { this.sold = this.total - available; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public double getPrice() { return price; }
    public int getSold() { return sold; }
    public int getRemaining() { return total - sold; }
}
