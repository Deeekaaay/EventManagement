import java.io.IOException;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import model.Model;
import controller.LoginController;
import controller.AdminController;


public class Main extends Application {
	private Model model;

	@Override
	public void init() {
		model = new Model();
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			model.setup();
			FXMLLoader loader;
			if (model.getCurrentUser() != null &&
				model.getCurrentUser().getRole() != null &&
				model.getCurrentUser().getRole().equals("admin")) {
				loader = new FXMLLoader(getClass().getResource("/view/AdminView.fxml"));
				AdminController adminController = new AdminController(primaryStage, model);
				loader.setController(adminController);
				Pane root = loader.load();
				adminController.showStage(root);
			} else {
				loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
				LoginController loginController = new LoginController(primaryStage, model);
				loader.setController(loginController);
				GridPane root = loader.load();
				loginController.showStage(root);
			}
		} catch (IOException | SQLException | RuntimeException e) {
			Scene scene = new Scene(new Label(e.getMessage()), 200, 100);
			primaryStage.setTitle("Error");
			primaryStage.setScene(scene);
			primaryStage.show();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
