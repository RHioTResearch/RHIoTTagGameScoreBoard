package org.jboss.rhiot.scoreboard;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javafx.application.Application;

/**
 * Created by starksm on 6/13/16.
 */
public class Main extends Application {
    private MainController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("RHIoTTag Game ScoreBoard");
        URL fxml = getClass().getResource("main.fxml");
        FXMLLoader loader = new FXMLLoader(fxml);
        Parent root = loader.load();
        this.controller = loader.getController();
        System.out.printf("Loaded controller: %s\n", controller);
        //
        primaryStage.setScene(new Scene(root, 1080, 640));
        primaryStage.setOnCloseRequest(this::handleClose);
        primaryStage.show();
    }

    /**
     * Close the controller if the user closes the window
     * @param we
     */
    private void handleClose(WindowEvent we) {
        controller.close();
    }

}
