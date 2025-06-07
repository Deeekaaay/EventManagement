package controller;

import java.io.IOException;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Model;
import model.User;

public class LoginController {
	@FXML
	private TextField name;
	@FXML
	private PasswordField password;
	@FXML
	private Label message;
	@FXML
	private Button login;
	@FXML
	private Button signup;

	private Model model;
	private Stage stage;
	
	public LoginController(Stage stage, Model model) {
		this.stage = stage;
		this.model = model;
	}
	
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
	
	@FXML
	public void initialize() {
		login.setOnAction(event -> {
			if (!name.getText().isEmpty() && !password.getText().isEmpty()) {
				User user = null;
				try {
					System.out.println("DEBUG: Attempting login for username='" + name.getText() + "' password='" + password.getText() + "'");
					user = model.getUserDao().getUser(name.getText(), password.getText());
					System.out.println("DEBUG: User found: " + (user != null ? user.getUsername() + ", role=" + user.getRole() : "null"));
					if (user != null) {
						model.setCurrentUser(user);
						Stage currentStage = (Stage) login.getScene().getWindow();
						if (user.getRole() != null && user.getRole().equals("admin")) {
							showAdminDashboard();
							currentStage.hide();
						} else {
							FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
							HomeController homeController = new HomeController();
							loader.setController(homeController);
							VBox root;
							try {
								root = loader.load();
								homeController.setStageAndModel(stage, model);
								homeController.showStage(root);
								currentStage.hide(); // Hide the login window after successful login
							} catch (IOException e) {
								message.setText(e.getMessage());
							}
						}
					} else {
						message.setText("Wrong username or password");
						message.setTextFill(Color.RED);
					}
				} catch (SQLException e) {
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
		
		signup.setOnAction(event -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SignupView.fxml"));
				
				// Customize controller instance
				SignupController signupController =  new SignupController(stage, model);

				loader.setController(signupController);
				VBox root = loader.load();
				
				signupController.showStage(root);
				
				message.setText("");
				name.clear();
				password.clear();
				
				stage.close();
			} catch (IOException e) {
				message.setText(e.getMessage());
			}});
	}
	
	public void showStage(Pane root) {
		Scene scene = new Scene(root, 500, 300);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Welcome");
		stage.show();
	}
}

