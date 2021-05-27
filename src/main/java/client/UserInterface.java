package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class UserInterface extends Application {

    public static void main(String[] args) {
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws IOException, InterruptedException {
        Parent root = FXMLLoader.load(getClass().getResource("/UserInterface.fxml"));
        primaryStage.setTitle("Cloud Storage by E.Petrov");
        primaryStage.setScene(new Scene(root, 750, 500));
        primaryStage.show();
    }
}
