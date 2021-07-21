package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StartClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/UserInterface.fxml"));
        primaryStage.setTitle("Cloud Storage by E.Petrov");
        primaryStage.setScene(new Scene(root, 1200, 600));
        primaryStage.show();
    }
}
