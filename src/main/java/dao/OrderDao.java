package dao;

import model.Order;
import java.util.List;

public interface OrderDao {
    void addOrder(Order order, int userId) throws Exception;
    List<Order> getAllOrders() throws Exception;
    List<Order> getOrdersForUser(int userId) throws Exception;
}
