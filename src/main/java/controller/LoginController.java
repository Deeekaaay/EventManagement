package controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Model;
import model.User;

/**
 * Controller for the Login screen.
 * Handles user authentication and navigation to signup or dashboard views.
 */
public class LoginController {
    @FXML private TextField name;
    @FXML private PasswordField password;
    @FXML private Label message;
    @FXML private Button login;
    @FXML private Button signup;

    private final Model model;
    private final Stage stage;

    /**
     * Constructs the LoginController with the given stage and model.
     * @param stage The primary stage.
     * @param model The application model.
     */
    public LoginController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    /**
     * Shows the admin dashboard in a new window.
     */
    private void showAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminView.fxml"));
            AdminController adminController = new AdminController(new Stage(), model);
            loader.setController(adminController);
            Pane root = loader.load();
            adminController.showStage(root);
            // Do NOT close the login stage for admin
        } catch (IOException e) {
            message.setText(e.getMessage());
        }
    }

    /**
     * Initializes the login and signup button actions.
     */
    @FXML
    public void initialize() {
        // Login button action
        login.setOnAction(_ -> {
            if (!name.getText().isEmpty() && !password.getText().isEmpty()) {
                try {
                    User user = model.getUserDao().getUser(name.getText(), password.getText());
                    if (user != null) {
                        model.setCurrentUser(user);
                        Stage currentStage = (Stage) login.getScene().getWindow();
                        if ("admin".equals(user.getRole())) {
                            showAdminDashboard();
                            currentStage.hide();
                        } else {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
                            HomeController homeController = new HomeController();
                            loader.setController(homeController);
                            VBox root = loader.load();
                            homeController.setStageAndModel(stage, model);
                            homeController.showStage(root);
                            currentStage.hide(); // Hide the login window after successful login
                        }
                    } else {
                        message.setText("Wrong username or password");
                        message.setTextFill(Color.RED);
                    }
                } catch (Exception e) {
                    message.setText(e.getMessage());
                    message.setTextFill(Color.RED);
                }
            } else {
                message.setText("Empty username or password");
                message.setTextFill(Color.RED);
            }
            name.clear();
            password.clear();
        });

        // Signup button action
        signup.setOnAction(_ -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SignupView.fxml"));
                SignupController signupController = new SignupController(stage, model);
                loader.setController(signupController);
                VBox root = loader.load();
                signupController.showStage(root);
                message.setText("");
                name.clear();
                password.clear();
                stage.close();
            } catch (IOException e) {
                message.setText(e.getMessage());
            }
        });
    }

    /**
     * Shows the login stage with the given root pane.
     * @param root The root pane to display.
     */
    public void showStage(Pane root) {
        Scene scene = new Scene(root, 500, 300);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Welcome");
        stage.show();
    }
}

