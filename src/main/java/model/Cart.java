package model;

import java.util.HashMap;
import java.util.Map;

public class Cart {
    private Map<Event, Integer> items = new HashMap<>();

    public void addToCart(Event event, int quantity) {
        items.put(event, items.getOrDefault(event, 0) + quantity);
    }

    public void updateQuantity(Event event, int quantity) {
        if (quantity <= 0) {
            items.remove(event);
        } else {
            items.put(event, quantity);
        }
    }

    public void removeFromCart(Event event) {
        items.remove(event);
    }

    public Map<Event, Integer> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
    }
}
