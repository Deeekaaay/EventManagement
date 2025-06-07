package dao;

import java.util.List;
import model.Event;

public interface EventDao {
    List<Event> getAllEvents();
    List<String> getAllEventTitles();
    List<Event> getEventsByTitle(String title);
    void addEvent(Event event) throws Exception;
    void updateEvent(Event event) throws Exception;
    void deleteEvent(int eventId) throws Exception;
    void setEventEnabled(int eventId, boolean enabled) throws Exception;
    boolean eventExists(Event event) throws Exception;
    Event getEventById(int eventId);
}
