package dao;

import java.sql.*;
import java.util.*;
import model.Event;

public class EventDaoImpl implements EventDao {
    private final Connection conn;

    public EventDaoImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                events.add(mapRowToEvent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    private Event mapRowToEvent(ResultSet rs) throws SQLException {
        return new Event(
            rs.getInt("event_id"),
            rs.getString("title"),
            rs.getString("date"),
            rs.getString("location"),
            rs.getDouble("price"),
            rs.getInt("total_seats"),
            rs.getInt("available_seats"),
            rs.getInt("enabled") == 1
        );
    }

    @Override
    public void addEvent(Event event) throws Exception {
        if (eventExists(event)) throw new Exception("Duplicate event");
        String sql = "INSERT INTO events (title, date, location, price, total_seats, available_seats, enabled) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDay());
            ps.setString(3, event.getVenue());
            ps.setDouble(4, event.getPrice());
            ps.setInt(5, event.getTotal());
            ps.setInt(6, event.getAvailableSeats());
            ps.setInt(7, event.isEnabled() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateEvent(Event event) throws Exception {
        if (eventExists(event)) throw new Exception("Duplicate event");
        String sql = "UPDATE events SET title=?, date=?, location=?, price=?, total_seats=?, available_seats=?, enabled=? WHERE event_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDay());
            ps.setString(3, event.getVenue());
            ps.setDouble(4, event.getPrice());
            ps.setInt(5, event.getTotal());
            ps.setInt(6, event.getAvailableSeats());
            ps.setInt(7, event.isEnabled() ? 1 : 0);
            ps.setInt(8, event.getEventId());
            ps.executeUpdate();
        }
    }

    @Override
    public boolean eventExists(Event event) throws Exception {
        String sql = "SELECT COUNT(*) FROM events WHERE title=? AND date=? AND location=? AND event_id != ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDay());
            ps.setString(3, event.getVenue());
            ps.setInt(4, event.getEventId() == 0 ? -1 : event.getEventId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> getAllEventTitles() {
        List<String> titles = new ArrayList<>();
        String sql = "SELECT DISTINCT title FROM events";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                titles.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return titles;
    }

    @Override
    public List<Event> getEventsByTitle(String title) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE title = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    @Override
    public void deleteEvent(int eventId) throws Exception {
        String sql = "DELETE FROM events WHERE event_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.executeUpdate();
        }
    }

    @Override
    public void setEventEnabled(int eventId, boolean enabled) throws Exception {
        String sql = "UPDATE events SET enabled=? WHERE event_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enabled ? 1 : 0);
            ps.setInt(2, eventId);
            ps.executeUpdate();
        }
    }

    @Override
    public Event getEventById(int eventId) {
        String sql = "SELECT * FROM events WHERE event_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEvent(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
