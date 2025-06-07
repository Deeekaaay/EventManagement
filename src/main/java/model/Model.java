package model;

import dao.Database;
import dao.EventDao;
import dao.EventDaoImpl;
import dao.OrderDao;
import dao.OrderDaoImpl;
import dao.UserDao;
import dao.UserDaoImpl;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Model {
	private UserDao userDao;
	private EventDao eventDao;
	private OrderDao orderDao;
	private User currentUser; 
	private Cart cart;
	private Connection dbConn;
	
	public Model() {
		userDao = new UserDaoImpl();
		cart = new Cart();
		try {
			dbConn = Database.getInstance().getConnection();
			eventDao = new EventDaoImpl(dbConn);
			orderDao = new OrderDaoImpl(dbConn, eventDao);
			// Load initial events from events.dat if database is empty
			if (eventDao.getAllEvents().isEmpty()) {
				loadInitialEventsFromFile();
			}
		} catch (Exception e) {
			// Error suppressed
		}
	}
	
	private void loadInitialEventsFromFile() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
			getClass().getResourceAsStream("/events.dat")))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(";");
				if (parts.length == 6) {
					String title = parts[0].trim();
					String venue = parts[1].trim();
					String day = parts[2].trim();
					double price = Double.parseDouble(parts[3].trim());
					int sold = Integer.parseInt(parts[4].trim());
					int total = Integer.parseInt(parts[5].trim());
					int available = total - sold;
					Event event = new Event(title, day, venue, price, total, available, true);
					try {
						eventDao.addEvent(event);
					} catch (Exception ex) {
						// Ignore duplicates or errors
					}
				}
			}
		} catch (Exception | Error e) {
			// Error suppressed
		}
	}
	
	public void setup() throws SQLException {
		userDao.setup();
	}
	public UserDao getUserDao() {
		return userDao;
	}
	
	public User getCurrentUser() {
		return this.currentUser;
	}
	
	public void setCurrentUser(User user) {
		currentUser = user;
	}
	public Cart getCart() {
		return cart;
	}
	public void clearCart() {
		cart.clear();
	}
	
	// --- ORDER DB LOGIC ---
	public void addOrder(Order order) {
		try {
			int userId = -1;
			if (currentUser != null && currentUser.getUserId() > 0) {
				userId = currentUser.getUserId();
			} else if (currentUser != null && currentUser.getUsername() != null) {
				userId = getUserIdByUsername(currentUser.getUsername());
			} else if (order.getCustomerName() != null) {
				userId = getUserIdByPreferredName(order.getCustomerName());
			}
			if (userId <= 0) throw new SQLException("Could not determine user_id for order");
			orderDao.addOrder(order, userId);
		} catch (Exception e) {
			// Error suppressed
		}
	}

	public List<Order> getOrders() {
		try {
			return orderDao.getAllOrders();
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	public List<Order> getOrdersForCurrentUser() {
		if (currentUser == null) return new ArrayList<>();
		try {
			return orderDao.getOrdersForUser(currentUser.getUserId());
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	// Helper to get user_id by username (unique)
	private int getUserIdByUsername(String username) throws SQLException {
		String sql = "SELECT user_id FROM users WHERE username = ?";
		PreparedStatement ps = dbConn.prepareStatement(sql);
		ps.setString(1, username);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) return rs.getInt("user_id");
		return -1;
	}
	// Helper to get user_id by preferredName (not recommended, but fallback)
	private int getUserIdByPreferredName(String preferredName) throws SQLException {
		String sql = "SELECT user_id FROM users WHERE preferred_name = ?";
		PreparedStatement ps = dbConn.prepareStatement(sql);
		ps.setString(1, preferredName);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) return rs.getInt("user_id");
		return -1;
	}

	public String getNextOrderNumber() {
		try {
			String sql = "SELECT MAX(order_id) as max_id FROM orders";
			PreparedStatement ps = dbConn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			int next = 1;
			if (rs.next()) next = rs.getInt("max_id") + 1;
			return String.format("%04d", next);
		} catch (Exception e) {
			return "0001";
		}
	}
	public EventDao getEventDao() {
		return eventDao;
	}

	// Event management methods for admin
	public List<Event> getAllEvents(boolean includeDisabled) {
		List<Event> all = eventDao.getAllEvents();
		if (includeDisabled) return all;
		List<Event> enabled = new ArrayList<>();
		for (Event e : all) if (e.isEnabled()) enabled.add(e);
		return enabled;
	}
	public List<String> getAllEventTitles() {
		return eventDao.getAllEventTitles();
	}
	public List<Event> getEventsByTitle(String title) {
		return eventDao.getEventsByTitle(title);
	}
	public void addEvent(Event event) throws Exception {
		eventDao.addEvent(event);
	}
	public void updateEvent(Event event) throws Exception {
		eventDao.updateEvent(event);
	}
	public void deleteEvent(int eventId) throws Exception {
		eventDao.deleteEvent(eventId);
	}
	public void setEventEnabled(int eventId, boolean enabled) throws Exception {
		eventDao.setEventEnabled(eventId, enabled);
	}
	public boolean eventExists(Event event) throws Exception {
		return eventDao.eventExists(event);
	}
}
