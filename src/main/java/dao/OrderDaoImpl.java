package dao;

import model.Order;
import model.OrderItem;
import model.Event;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDaoImpl implements OrderDao {
    private final Connection dbConn;
    private final EventDao eventDao;

    public OrderDaoImpl(Connection dbConn, EventDao eventDao) {
        this.dbConn = dbConn;
        this.eventDao = eventDao;
    }

    @Override
    public void addOrder(Order order, int userId) throws Exception {
        dbConn.setAutoCommit(false);
        String insertOrder = "INSERT INTO orders (user_id, order_date, total_price) VALUES (?, ?, ?)";
        PreparedStatement psOrder = dbConn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS);
        psOrder.setInt(1, userId);
        psOrder.setString(2, order.getDateTime().toString());
        psOrder.setDouble(3, order.getTotalPrice());
        psOrder.executeUpdate();
        ResultSet rs = psOrder.getGeneratedKeys();
        int orderId = -1;
        if (rs.next()) orderId = rs.getInt(1);
        String insertItem = "INSERT INTO order_items (order_id, event_id, quantity, price_per_ticket) VALUES (?, ?, ?, ?)";
        PreparedStatement psItem = dbConn.prepareStatement(insertItem);
        for (OrderItem item : order.getItems()) {
            psItem.setInt(1, orderId);
            psItem.setInt(2, item.getEvent().getEventId());
            psItem.setInt(3, item.getQuantity());
            psItem.setDouble(4, item.getEvent().getPrice());
            psItem.addBatch();
        }
        psItem.executeBatch();
        // Update available_seats for each event
        for (OrderItem item : order.getItems()) {
            String updateEvent = "UPDATE events SET available_seats = available_seats - ? WHERE event_id = ?";
            try (PreparedStatement psUpdate = dbConn.prepareStatement(updateEvent)) {
                psUpdate.setInt(1, item.getQuantity());
                psUpdate.setInt(2, item.getEvent().getEventId());
                psUpdate.executeUpdate();
            }
        }
        dbConn.commit();
    }

    @Override
    public List<Order> getAllOrders() throws Exception {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.order_id, o.order_date, o.total_price, u.username, u.preferred_name, u.user_id FROM orders o JOIN users u ON o.user_id = u.user_id ORDER BY o.order_id DESC";
        PreparedStatement ps = dbConn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int orderId = rs.getInt("order_id");
            LocalDateTime dateTime = LocalDateTime.parse(rs.getString("order_date"));
            double total = rs.getDouble("total_price");
            String username = rs.getString("username");
            List<OrderItem> items = getOrderItems(orderId);
            Order order = new Order(String.format("%04d", orderId), dateTime, items, total, username);
            orders.add(order);
        }
        return orders;
    }

    @Override
    public List<Order> getOrdersForUser(int userId) throws Exception {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.order_id, o.order_date, o.total_price, u.preferred_name FROM orders o JOIN users u ON o.user_id = u.user_id WHERE o.user_id = ? ORDER BY o.order_id DESC";
        PreparedStatement ps = dbConn.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int orderId = rs.getInt("order_id");
            LocalDateTime dateTime = LocalDateTime.parse(rs.getString("order_date"));
            double total = rs.getDouble("total_price");
            String customerName = rs.getString("preferred_name");
            List<OrderItem> items = getOrderItems(orderId);
            Order order = new Order(String.format("%04d", orderId), dateTime, items, total, customerName);
            orders.add(order);
        }
        return orders;
    }

    private List<OrderItem> getOrderItems(int orderId) throws Exception {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.event_id, oi.quantity, oi.price_per_ticket FROM order_items oi WHERE oi.order_id = ?";
        PreparedStatement ps = dbConn.prepareStatement(sql);
        ps.setInt(1, orderId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int eventId = rs.getInt("event_id");
            int qty = rs.getInt("quantity");
            Event event = eventDao.getEventById(eventId);
            if (event != null) {
                items.add(new OrderItem(event, qty));
            }
        }
        return items;
    }
}
