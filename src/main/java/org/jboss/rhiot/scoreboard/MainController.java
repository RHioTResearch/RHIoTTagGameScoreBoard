package org.jboss.rhiot.scoreboard;

import java.util.Date;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

/**
 * Created by starksm on 6/17/16.
 */
public class MainController {
    @FXML
    private Label highScore;
    @FXML
    private GridPane gridPane;
    @FXML
    private TableView<GameScore> scoreTableView;
    @FXML
    private TableColumn<GameScore, String> nameColumn;
    @FXML
    private TableColumn<GameScore, String> addressColumn;
    @FXML
    private TableColumn<GameScore, Integer> gatewayColumn;
    @FXML
    private TableColumn<GameScore, Date> timeColumn;
    @FXML
    private TableColumn<GameScore, Integer> shotsColumn;
    @FXML
    private TableColumn<GameScore, Integer> scoreColumn;

    private SortedList<GameScore> gameScores = new SortedList<>(FXCollections.observableArrayList(), new GameScoreComparator());

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        addressColumn.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        gatewayColumn.setCellValueFactory(cellData -> cellData.getValue().gatewayProperty().asObject());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        shotsColumn.setCellValueFactory(cellData -> cellData.getValue().shotsProperty().asObject());
        scoreColumn.setCellValueFactory(cellData -> cellData.getValue().scoreProperty().asObject());

        scoreTableView.setItems(gameScores);
    }

    public ObservableList<GameScore> getGameScores() {
        return gameScores;
    }

    public void setGameScores(ObservableList<GameScore> gameScores) {
        SortedList<GameScore> sortedList = new SortedList<>(gameScores, new GameScoreComparator());
        this.gameScores = sortedList;
        scoreTableView.setItems(sortedList);
    }

    public void addGame(GameScore gameScore) {
        gameScores.add(gameScore);
    }
}
