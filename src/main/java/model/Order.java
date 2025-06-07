package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Order implements Serializable {
    private String orderNumber; // 4-digit string
    private LocalDateTime dateTime;
    private List<OrderItem> items;
    private double totalPrice;
    private String customerName;

    public Order(String orderNumber, LocalDateTime dateTime, List<OrderItem> items, double totalPrice, String customerName) {
        this.orderNumber = orderNumber;
        this.dateTime = dateTime;
        this.items = items;
        this.totalPrice = totalPrice;
        this.customerName = customerName;
    }

    // For backward compatibility
    public Order(String orderNumber, LocalDateTime dateTime, List<OrderItem> items, double totalPrice) {
        this(orderNumber, dateTime, items, totalPrice, null);
    }

    public String getOrderNumber() { return orderNumber; }
    public LocalDateTime getDateTime() { return dateTime; }
    public List<OrderItem> getItems() { return items; }
    public double getTotalPrice() { return totalPrice; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
