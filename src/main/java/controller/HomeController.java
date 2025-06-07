package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import java.util.List;
import model.Event;
import model.Model;
import model.Order;
import model.OrderItem;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

@SuppressWarnings({"unused", "unchecked"})
public class HomeController {
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
    @FXML private Button addToCartBtn;
    @FXML private Button updateCartBtn;
    @FXML private Button removeFromCartBtn;
    @FXML private Button viewCartBtn;
    @FXML private Button checkoutBtn;
    @FXML private TextField quantityField;
    @FXML private Label cartMessage;
    @FXML private Button viewOrdersBtn;
    @FXML private Button exportOrdersBtn;

    private Event selectedEvent;

    public HomeController() {
        // No-arg constructor for FXML loader
    }

    public void setStageAndModel(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.model = model;
        // Restore all event handlers and validation logic
        refreshView();
        eventTable.getSelectionModel().selectedItemProperty().addListener((_, __, newValue) -> {
            selectedEvent = newValue;
        });
        addToCartBtn.setOnAction(_ -> handleAddToCart());
        updateCartBtn.setOnAction(_ -> handleUpdateCart());
        removeFromCartBtn.setOnAction(_ -> handleRemoveFromCart());
        viewCartBtn.setOnAction(_ -> handleViewCart());
        checkoutBtn.setOnAction(_ -> handleCheckout());
        viewOrdersBtn.setOnAction(_ -> handleViewOrders());
        exportOrdersBtn.setOnAction(_ -> handleExportOrders());
        checkoutBtn.setDisable(model.getCart().getItems().isEmpty());
        eventTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedEvent = newValue;
            if (selectedEvent == null || !model.getCart().getItems().containsKey(selectedEvent)) {
                removeFromCartBtn.setDisable(true);
            } else {
                removeFromCartBtn.setDisable(false);
            }
        });
        removeFromCartBtn.setDisable(true); // Initially disabled
    }

    /**
     * Refreshes the Home view with the current user's name and events from the model.
     */
    public void refreshView() {
        if (model == null || model.getCurrentUser() == null) return;
        // Personal welcome
        welcomeLabel.setText("Welcome, " + model.getCurrentUser().getPreferredName() + "!");

        // Set up table columns (only once)
        titleCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getTitle()));
        venueCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getVenue()));
        dayCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDay()));
        priceCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getPrice()));
        soldCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getSold()));
        totalCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getTotal()));
        remainingCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getRemaining()));

        // Load events from the model
        List<Event> events = model.getAllEvents(false);
        eventTable.setItems(FXCollections.observableArrayList(events));
    }

    @FXML
    public void initialize() {
        // Do not use model-dependent logic here
    }

    private void handleAddToCart() {
        if (selectedEvent == null) {
            cartMessage.setText("Select an event first.");
            return;
        }
        // Prevent adding events for days earlier than today
        String today = getTodayDayString();
        int todayIdx = getDayIndex(today);
        int eventIdx = getDayIndex(selectedEvent.getDay());
        if (eventIdx < todayIdx) {
            cartMessage.setText("Cannot add past events to cart.");
            return;
        }
        int qty = parseQuantity();
        if (qty <= 0) {
            cartMessage.setText("Enter a valid quantity.");
            return;
        }
        int available = selectedEvent.getRemaining();
        int inCart = model.getCart().getItems().getOrDefault(selectedEvent, 0);
        if (qty + inCart > available) {
            cartMessage.setText("Not enough seats available. Only " + (available - inCart) + " left.");
            return;
        }
        model.getCart().addToCart(selectedEvent, qty);
        cartMessage.setText("Added to cart.");
        quantityField.setText(""); // Reset quantity field after adding
        checkoutBtn.setDisable(model.getCart().getItems().isEmpty());
        removeFromCartBtn.setDisable(!model.getCart().getItems().containsKey(selectedEvent));
    }

    private void handleUpdateCart() {
        if (selectedEvent == null) {
            cartMessage.setText("Select an event first.");
            return;
        }
        int qty = parseQuantity();
        if (qty < 0) {
            cartMessage.setText("Enter a valid quantity.");
            return;
        }
        int available = selectedEvent.getRemaining();
        if (qty > available) {
            cartMessage.setText("Not enough seats available. Only " + available + " left.");
            return;
        }
        model.getCart().updateQuantity(selectedEvent, qty);
        cartMessage.setText(qty == 0 ? "Removed from cart." : "Cart updated.");
        checkoutBtn.setDisable(model.getCart().getItems().isEmpty());
        removeFromCartBtn.setDisable(selectedEvent == null || !model.getCart().getItems().containsKey(selectedEvent));
    }

    private void handleRemoveFromCart() {
        if (selectedEvent == null) {
            cartMessage.setText("Select an event first.");
            return;
        }
        model.getCart().removeFromCart(selectedEvent);
        cartMessage.setText("Removed from cart.");
        checkoutBtn.setDisable(model.getCart().getItems().isEmpty());
        removeFromCartBtn.setDisable(true);
    }

    private void handleViewCart() {
        StringBuilder sb = new StringBuilder();
        model.getCart().getItems().forEach((event, qty) -> {
            sb.append(event.getTitle()).append(" (" + event.getDay() + "): ").append(qty).append("\n");
        });
        if (sb.length() == 0) sb.append("Cart is empty.");
        Alert alert = new Alert(Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
        alert.setHeaderText("Your Cart");
        alert.showAndWait();
    }

    private void handleCheckout() {
        if (model.getCart().getItems().isEmpty()) {
            cartMessage.setText("Cart is empty. Cannot checkout.");
            checkoutBtn.setDisable(true);
            return;
        } else {
            checkoutBtn.setDisable(false);
        }
        StringBuilder warnings = new StringBuilder();
        // Check for seat availability
        model.getCart().getItems().forEach((event, qty) -> {
            if (qty > event.getRemaining()) {
                warnings.append(event.getTitle()).append(" (" + event.getDay() + ") - only ")
                    .append(event.getRemaining()).append(" left.\n");
            }
        });
        // Check for event date validity
        String today = getTodayDayString();
        int todayIdx = getDayIndex(today);
        for (Event event : model.getCart().getItems().keySet()) {
            int eventIdx = getDayIndex(event.getDay());
            if (eventIdx < todayIdx) {
                warnings.append(event.getTitle()).append(" (" + event.getDay() + ") - cannot book past events.\n");
            }
        }
        if (warnings.length() > 0) {
            cartMessage.setText("Checkout failed. Issues:\n" + warnings);
            return;
        }
        // Calculate total price
        double total = model.getCart().getItems().entrySet().stream()
            .mapToDouble(e -> e.getKey().getPrice() * e.getValue()).sum();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Total price: $" + String.format("%.2f", total) + "\nProceed to payment?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Checkout");
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) {
            cartMessage.setText("Checkout cancelled.");
            return;
        }
        // Ask for 6-digit confirmation code
        TextInputDialog codeDialog = new TextInputDialog();
        codeDialog.setHeaderText("Enter 6-digit confirmation code sent to your phone/email:");
        codeDialog.setContentText("Confirmation code:");
        String code = codeDialog.showAndWait().orElse("");
        if (!code.matches("\\d{6}")) {
            cartMessage.setText("Invalid confirmation code. Payment failed.");
            return;
        }
        // Simulate booking: update sold count in event objects (in-memory only)
        model.getCart().getItems().forEach((event, qty) -> {
            try {
                java.lang.reflect.Field soldField = event.getClass().getDeclaredField("sold");
                soldField.setAccessible(true);
                soldField.setInt(event, event.getSold() + qty);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        // Save order
        List<model.OrderItem> orderItems = new java.util.ArrayList<>();
        model.getCart().getItems().forEach((event, qty) -> {
            orderItems.add(new model.OrderItem(event, qty));
        });
        String orderNumber = model.getNextOrderNumber();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String customerName = model.getCurrentUser() != null ? model.getCurrentUser().getPreferredName() : "";
        model.Order order = new model.Order(orderNumber, now, orderItems, total, customerName);
        model.addOrder(order);
        model.clearCart();
        eventTable.refresh();
        cartMessage.setText("Checkout successful! Payment confirmed. Order No: " + orderNumber);
    }

    private void handleViewOrders() {
        // Use DB-backed method to get only current user's orders
        List<Order> userOrders = model.getOrdersForCurrentUser();
        if (userOrders.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No orders found.", ButtonType.OK);
            alert.setHeaderText("Order History");
            alert.showAndWait();
            return;
        }
        TableView<Order> orderTable = new TableView<>();
        TableColumn<Order, String> orderNumCol = new TableColumn<>("Order No");
        orderNumCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getOrderNumber()));
        TableColumn<Order, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDateTime().toString()));
        TableColumn<Order, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getTotalPrice()));
        orderTable.getColumns().addAll(orderNumCol, dateCol, totalCol);
        orderTable.setItems(FXCollections.observableArrayList(userOrders));
        orderTable.setPrefHeight(200);

        TableView<OrderItem> itemTable = new TableView<>();
        TableColumn<OrderItem, String> eventCol = new TableColumn<>("Event");
        eventCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getEvent().getTitle()));
        TableColumn<OrderItem, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getEvent().getDay()));
        TableColumn<OrderItem, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getQuantity()));
        TableColumn<OrderItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getEvent().getPrice()));
        itemTable.getColumns().addAll(eventCol, dayCol, qtyCol, priceCol);
        itemTable.setPrefHeight(150);

        orderTable.getSelectionModel().selectedItemProperty().addListener((_, __, newOrder) -> {
            if (newOrder != null) {
                itemTable.setItems(FXCollections.observableArrayList(newOrder.getItems()));
            } else {
                itemTable.setItems(FXCollections.observableArrayList());
            }
        });
        if (!userOrders.isEmpty()) {
            orderTable.getSelectionModel().selectFirst();
            itemTable.setItems(FXCollections.observableArrayList(userOrders.get(0).getItems()));
        }

        VBox vbox = new VBox(10, new Label("Your Orders:"), orderTable, new Label("Order Items:"), itemTable);
        vbox.setPrefWidth(600);
        vbox.setPrefHeight(400);
        Scene scene = new Scene(vbox);
        Stage popup = new Stage();
        popup.setTitle("Order History");
        popup.setScene(scene);
        popup.initOwner(stage);
        popup.showAndWait();
    }

    private void handleExportOrders() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Orders");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        Stage fileStage = new Stage();
        java.io.File file = fileChooser.showSaveDialog(fileStage);
        if (file == null) return;
        List<Order> orders = model.getOrders();
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            for (Order order : orders) {
                writer.println("Order No: " + order.getOrderNumber() + " | Date: " + order.getDateTime() + " | Total: $" + String.format("%.2f", order.getTotalPrice()));
                for (OrderItem item : order.getItems()) {
                    writer.println("   - " + item.getEvent().getTitle() + " (" + item.getEvent().getDay() + ") x " + item.getQuantity());
                }
                writer.println();
            }
            cartMessage.setText("Orders exported to: " + file.getAbsolutePath());
        } catch (Exception ex) {
            cartMessage.setText("Failed to export orders: " + ex.getMessage());
        }
    }

    private int parseQuantity() {
        try {
            return Integer.parseInt(quantityField.getText().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Home");
        stage.show();
    }

    private String getTodayDayString() {
        java.time.DayOfWeek day = java.time.LocalDate.now().getDayOfWeek();
        switch (day) {
            case MONDAY: return "Mon";
            case TUESDAY: return "Tue";
            case WEDNESDAY: return "Wed";
            case THURSDAY: return "Thu";
            case FRIDAY: return "Fri";
            case SATURDAY: return "Sat";
            case SUNDAY: return "Sun";
            default: return "";
        }
    }

    private int getDayIndex(String day) {
        switch (day) {
            case "Mon": return 0;
            case "Tue": return 1;
            case "Wed": return 2;
            case "Thu": return 3;
            case "Fri": return 4;
            case "Sat": return 5;
            case "Sun": return 6;
            default: return -1;
        }
    }
}
