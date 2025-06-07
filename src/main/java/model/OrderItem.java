package model;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private Event event;
    private int quantity;

    public OrderItem(Event event, int quantity) {
        this.event = event;
        this.quantity = quantity;
    }

    public Event getEvent() { return event; }
    public int getQuantity() { return quantity; }
}
