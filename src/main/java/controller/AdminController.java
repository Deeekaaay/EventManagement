package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.Event;
import javafx.stage.Stage;
import java.util.*;

public class AdminController {
    private Model model;
    private Stage stage;

    @FXML private Label welcomeLabel;
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> titleCol;
    @FXML private TableColumn<Event, String> venueCol;
    @FXML private TableColumn<Event, String> dayCol;
    @FXML private TableColumn<Event, Double> priceCol;
    @FXML private TableColumn<Event, Integer> soldCol;
    @FXML private TableColumn<Event, Integer> totalCol;
    @FXML private TableColumn<Event, Integer> remainingCol;
    @FXML private TableColumn<Event, Boolean> enableCol;
    @FXML private Button addEventBtn;
    @FXML private Button deleteEventBtn;
    @FXML private Button modifyEventBtn;
    @FXML private Button enableEventBtn;
    @FXML private Button disableEventBtn;
    @FXML private Button viewOrdersBtn;
    @FXML private Label adminMessage;
    @FXML private MenuBar adminMenuBar;
    @FXML private Menu adminAccountMenu;
    @FXML private MenuItem adminChangePasswordMenu;
    @FXML private MenuItem adminLogoutMenu;

    private Event selectedEvent;

    public AdminController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        if (model.getCurrentUser() != null && model.getCurrentUser().getRole() != null && model.getCurrentUser().getRole().equals("admin")) {
            welcomeLabel.setText("Welcome, Admin!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
        // Set up table columns
        titleCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getTitle()));
        venueCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getVenue()));
        dayCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDay()));
        priceCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getPrice()));
        soldCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getSold()));
        totalCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getTotal()));
        remainingCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getRemaining()));
        enableCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().isEnabled()));
        // Load grouped events
        eventTable.setItems(getGroupedEvents());
        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedEvent = newSel;
        });
        addEventBtn.setOnAction(e -> handleAddEvent());
        deleteEventBtn.setOnAction(e -> handleDeleteEvent());
        modifyEventBtn.setOnAction(e -> handleModifyEvent());
        enableEventBtn.setOnAction(e -> handleEnableEvent());
        disableEventBtn.setOnAction(e -> handleDisableEvent());
        viewOrdersBtn.setOnAction(e -> handleViewOrders());
        adminChangePasswordMenu.setOnAction(e -> handleAdminChangePassword());
        adminLogoutMenu.setOnAction(e -> handleAdminLogout());
    }

    private ObservableList<Event> getGroupedEvents() {
        List<Event> all = model.getAllEvents(true); // true = include disabled
        ObservableList<Event> result = FXCollections.observableArrayList();
        result.addAll(all);
        return result;
    }

    private void handleAddEvent() {
        // Show dialog to add event
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Add New Event (format: Title;Day;Venue;Price;TotalSeats;AvailableSeats)");
        dialog.setContentText("Enter details:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String[] parts = result.get().split(";");
            if (parts.length != 6) {
                adminMessage.setText("Invalid input format.");
                return;
            }
            try {
                String title = parts[0].trim();
                String day = parts[1].trim();
                String venue = parts[2].trim();
                double price = Double.parseDouble(parts[3].trim());
                int total = Integer.parseInt(parts[4].trim());
                int available = Integer.parseInt(parts[5].trim());
                boolean enabled = true;
                Event event = new Event(title, day, venue, price, total, available, enabled);
                if (model.eventExists(event)) {
                    adminMessage.setText("Duplicate event detected.");
                    return;
                }
                model.addEvent(event);
                eventTable.setItems(getGroupedEvents());
                adminMessage.setText("Event added.");
            } catch (Exception ex) {
                adminMessage.setText("Error: " + ex.getMessage());
            }
        }
    }

    private void handleDeleteEvent() {
        if (selectedEvent == null) {
            adminMessage.setText("Select an event to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete event '" + selectedEvent.getTitle() + "' (" + selectedEvent.getVenue() + ", " + selectedEvent.getDay() + ")?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Delete");
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                model.deleteEvent(selectedEvent.getEventId());
                eventTable.setItems(getGroupedEvents());
                adminMessage.setText("Event deleted.");
            } catch (Exception ex) {
                adminMessage.setText("Error: " + ex.getMessage());
            }
        }
    }

    private void handleModifyEvent() {
        if (selectedEvent == null) {
            adminMessage.setText("Select an event to modify.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(
            selectedEvent.getTitle() + ";" +
            selectedEvent.getDay() + ";" +
            selectedEvent.getVenue() + ";" +
            selectedEvent.getPrice() + ";" +
            selectedEvent.getTotal() + ";" +
            selectedEvent.getAvailableSeats()
        );
        dialog.setHeaderText("Modify Event (format: Title;Day;Venue;Price;TotalSeats;AvailableSeats)");
        dialog.setContentText("Edit details:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String[] parts = result.get().split(";");
            if (parts.length != 6) {
                adminMessage.setText("Invalid input format.");
                return;
            }
            try {
                String title = parts[0].trim();
                String day = parts[1].trim();
                String venue = parts[2].trim();
                double price = Double.parseDouble(parts[3].trim());
                int total = Integer.parseInt(parts[4].trim());
                int available = Integer.parseInt(parts[5].trim());
                if (total < 0 || available < 0 || available > total) {
                    adminMessage.setText("Invalid seat numbers.");
                    return;
                }
                Event updated = new Event(
                    selectedEvent.getEventId(),
                    title,
                    day,
                    venue,
                    price,
                    total,
                    available,
                    selectedEvent.isEnabled()
                );
                if (model.eventExists(updated)) {
                    adminMessage.setText("Duplicate event detected.");
                    return;
                }
                model.updateEvent(updated);
                eventTable.setItems(getGroupedEvents());
                adminMessage.setText("Event modified.");
            } catch (Exception ex) {
                adminMessage.setText("Error: " + ex.getMessage());
            }
        }
    }

    private void handleEnableEvent() {
        if (selectedEvent == null) {
            adminMessage.setText("Select an event to enable.");
            return;
        }
        try {
            model.setEventEnabled(selectedEvent.getEventId(), true);
            eventTable.setItems(getGroupedEvents());
            adminMessage.setText("Event enabled.");
        } catch (Exception ex) {
            adminMessage.setText("Error: " + ex.getMessage());
        }
    }

    private void handleDisableEvent() {
        if (selectedEvent == null) {
            adminMessage.setText("Select an event to disable.");
            return;
        }
        try {
            model.setEventEnabled(selectedEvent.getEventId(), false);
            eventTable.setItems(getGroupedEvents());
            adminMessage.setText("Event disabled.");
        } catch (Exception ex) {
            adminMessage.setText("Error: " + ex.getMessage());
        }
    }

    private void handleViewOrders() {
        // Show all orders for all users in a TableView inside a popup
        List<model.Order> orders = model.getOrders();
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("All Orders");
        dialog.setHeaderText("All Orders");

        TableView<model.Order> orderTable = new TableView<>();
        TableColumn<model.Order, String> orderNoCol = new TableColumn<>("Order No");
        orderNoCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getOrderNumber()));
        TableColumn<model.Order, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDateTime().toString()));
        TableColumn<model.Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.format("$%.2f", cell.getValue().getTotalPrice())));
        TableColumn<model.Order, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getCustomerName()));
        orderTable.getColumns().addAll(orderNoCol, dateCol, totalCol, customerCol);
        orderTable.setItems(FXCollections.observableArrayList(orders));

        // Table for order items
        TableView<model.OrderItem> itemTable = new TableView<>();
        TableColumn<model.OrderItem, String> eventCol = new TableColumn<>("Event");
        eventCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getEvent().getTitle()));
        TableColumn<model.OrderItem, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
            cell.getValue().getEvent().getDay() + ", " + cell.getValue().getEvent().getVenue()
        ));
        TableColumn<model.OrderItem, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getQuantity()));
        itemTable.getColumns().addAll(eventCol, detailsCol, qtyCol);

        Label selectedOrderLabel = new Label();
        selectedOrderLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 5 0;");
        orderTable.getSelectionModel().selectedItemProperty().addListener((_, __, newSel) -> {
            if (newSel != null) {
                itemTable.setItems(FXCollections.observableArrayList(newSel.getItems()));
                selectedOrderLabel.setText("Order Items for Order No: " + newSel.getOrderNumber());
            } else {
                itemTable.setItems(FXCollections.observableArrayList());
                selectedOrderLabel.setText("");
            }
        });
        if (!orders.isEmpty()) {
            orderTable.getSelectionModel().selectFirst();
            itemTable.setItems(FXCollections.observableArrayList(orders.get(0).getItems()));
            selectedOrderLabel.setText("Order Items for Order No: " + orders.get(0).getOrderNumber());
        }

        VBox vbox = new VBox(10, orderTable, selectedOrderLabel, itemTable);
        vbox.setPrefWidth(600);
        vbox.setPrefHeight(400);
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    private void handleAdminChangePassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Change Password");
        dialog.setContentText("Enter new password:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newPassword = result.get().trim();
            if (newPassword.isEmpty()) {
                adminMessage.setText("Password cannot be empty.");
                return;
            }
            try {
                boolean changed = model.getUserDao().changePassword(model.getCurrentUser().getUsername(), newPassword);
                if (changed) {
                    adminMessage.setText("Password changed successfully. Please use the new password next time.");
                } else {
                    adminMessage.setText("Failed to change password.");
                }
            } catch (Exception e) {
                adminMessage.setText("Error: " + e.getMessage());
            }
        }
    }

    private void handleAdminLogout() {
        stage.close();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            LoginController loginController = new LoginController(new Stage(), model);
            loader.setController(loginController);
            Pane root = loader.load();
            loginController.showStage(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Admin Dashboard");
        stage.show();
    }
}
