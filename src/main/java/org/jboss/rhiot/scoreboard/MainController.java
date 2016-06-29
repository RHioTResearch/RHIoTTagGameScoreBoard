package org.jboss.rhiot.scoreboard;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

/**
 * Created by starksm on 6/17/16.
 */
public class MainController implements ICloudListener {
    private static final Logger log = Logger.getLogger(MainController.class);
    @FXML
    private Label highScore;
    @FXML
    private Label activeGames;
    @FXML
    private Label completedGames;
    @FXML
    private GridPane gridPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab highScoresTab;
    @FXML
    private Tab activeScoresTab;
    @FXML
    private ImageView rhImage;
    @FXML
    private ImageView etImage;


    private CloudClient cloudClient;
    private int completedGameCount;
    private ActiveGameController activeGameController;
    private HighScoreController highScoreController;
    private TopScoresController topScoresController;

    public void close() {
        try {
            cloudClient.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endGame(Date time, int gateway, String name, String tagAddress, int hits, int score, boolean isNewHighScore) {
        GameScore gs = activeGameController.removeGame(tagAddress);
        if(gs != null) {
            gs.setTime(time);
            gs.setScore(score);
            gs.setShots(hits);
            gs.setTimeLeft(0);
            highScoreController.addGame(gs);
            if(topScoresController != null) {
                List<GameScore> topScores = highScoreController.getTopScores();
                topScoresController.updateTopScores(topScores);
            }
        } else {
            log.warn(String.format("Old endGame(%s, %d, %s, %s, %d, %d, %s)", time, gateway, name, tagAddress, hits, score, isNewHighScore));
        }
        completedGameCount ++;
        log.info(String.format("endGame(%s@GW%d), score=%d, isNewHighScore=%s", tagAddress, gateway, score, isNewHighScore));
        log.info("completedGameCount="+completedGameCount);
        if(gs != null) {
            Platform.runLater(() -> {
                if (isNewHighScore) {
                    highScore.setText(name + "@" + score);
                }
                completedGames.setText("" + completedGameCount);
                activeGames.setText(""+activeGameController.size());
            });
        } else {
            log.error("Failed to get game for tag address:"+tagAddress);
        }
    }

    @Override
    public void gameInfo(Date time, int gateway, String name, String tagAddress, int hits, int score, int shotsLeft, int shootingTimeLeft, int timeLeft) {
        GameScore gs = activeGameController.getGameScore(tagAddress);
        if(gs.getName().length() == 0) {
            gs.setName(name);
            gs.setTime(time);
            Platform.runLater(() -> activeGames.setText(""+activeGameController.size()));
        }
        gs.setGateway(gateway);
        gs.setTime(time);
        gs.setShots(hits);
        gs.setScore(score);
        gs.setTimeLeft(timeLeft);
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Load the active game scores view
        URL activegames = getClass().getResource("activegames.fxml");
        FXMLLoader loader = new FXMLLoader(activegames);
        try {
            Parent root = loader.load();
            activeGameController = loader.getController();
            activeScoresTab.setContent(root);
        } catch (Exception e) {
            log.error("Failed to load activegames.fxml", e);
        }

        // Load the high game scores view
        URL highscores = getClass().getResource("highscores.fxml");
        FXMLLoader loaderHS = new FXMLLoader(highscores);
        try {
            Parent root = loaderHS.load();
            highScoreController = loaderHS.getController();
            highScoresTab.setContent(root);
        } catch (Exception e) {
            log.error("Failed to load highscores.fxml", e);
        }

        // Load the banner images
        Image img = new Image("images/bg-banner-main.png");
        rhImage.setImage(img);
        img = new Image("images/Eurotech-Logo_800x141.png");
        etImage.setImage(img);

        tabPane.getSelectionModel().select(2);

        cloudClient = new CloudClient();
        Thread t = new Thread(this::startCloudClient, "CloudClientInit");
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void gameReset() {
        completedGameCount = 0;
        highScore.setText("");
        activeGames.setText("");
        completedGames.setText("");

        highScoreController.clear();
        activeGameController.clear();
    }

    @FXML
    private void gameTopScores() {
        if(topScoresController == null) {
            URL fxml = getClass().getResource("topscores.fxml");
            FXMLLoader loader = new FXMLLoader(fxml);
            try {
                Pane scorePane = loader.load();
                topScoresController = loader.getController();
                List<GameScore> topScores = highScoreController.getTopScores();
                log.info(topScores);
                topScoresController.updateTopScores(topScores);
                Stage newStage = new Stage();
                newStage.setScene(new Scene(topScoresController.getGridPane(), 1080, 1080));
                newStage.initModality(Modality.WINDOW_MODAL);
                newStage.setTitle("Game Top Scores");
                newStage.showAndWait();
            } catch (IOException e) {
                ErrorDialog.displayErrorDirect("TopScores Load Failure", "TopScores failed to load", e.getMessage(), e);
            }
        }
        else {
            List<GameScore> topScores = highScoreController.getTopScores();
            log.info(topScores);
            topScoresController.updateTopScores(topScores);
            Scene scene = topScoresController.getGridPane().getScene();
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.initModality(Modality.WINDOW_MODAL);
            newStage.setTitle("Game Top Scores");
            newStage.showAndWait();
        }
    }
    @FXML
    private void gameSave() {
    }
    @FXML
    private void gameClose() {
        Stage stage = (Stage) gridPane.getScene().getWindow();
        stage.close();
    }

    private void startCloudClient() {
        try {
            log.info("Attempting to start RHIoTTag game scoreboard");
            cloudClient.start(this);
        } catch (Exception e) {
            // Display the error
            ErrorDialog.displayError("Cloud Connection Error", "Could not start the cloud client", "", e);
        }
    }
}
