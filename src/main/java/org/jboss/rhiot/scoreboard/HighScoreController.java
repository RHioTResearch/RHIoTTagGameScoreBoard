package org.jboss.rhiot.scoreboard;

import java.util.List;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Created by starksm on 6/20/16.
 */
public class HighScoreController {
    @FXML
    private TableView<GameScore> scoreTableView;
    @FXML
    private TableColumn<GameScore, String> nameColumn;
    @FXML
    private TableColumn<GameScore, String> addressColumn;
    @FXML
    private TableColumn<GameScore, Integer> gatewayColumn;
    @FXML
    private TableColumn<GameScore, String> timeColumn;
    @FXML
    private TableColumn<GameScore, Integer> shotsColumn;
    @FXML
    private TableColumn<GameScore, Integer> scoreColumn;
    /** Create a sorted list of game scores that updates when the game score changes */
    private ObservableList<GameScore> gameScores = FXCollections.observableArrayList(gs -> new Observable[]{gs.scoreProperty()});
    private SortedList<GameScore> gameScoresSorted = new SortedList<>(gameScores, new GameScoreComparator());

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        addressColumn.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        gatewayColumn.setCellValueFactory(cellData -> cellData.getValue().gatewayProperty().asObject());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeStringProperty());
        shotsColumn.setCellValueFactory(cellData -> cellData.getValue().shotsProperty().asObject());
        scoreColumn.setCellValueFactory(cellData -> cellData.getValue().scoreProperty().asObject());

        //gameScoresSorted.comparatorProperty().bind(scoreTableView.comparatorProperty());
        scoreTableView.setItems(gameScoresSorted);
        scoreTableView.sort();
    }

    public void addGame(GameScore gs) {
        gameScores.add(gs);
        scoreTableView.sort();
    }
    public void clear() {
        gameScores.clear();
    }

    /**
     * Return the top 9 scores
     * @return
     */
    public List<GameScore> getTopScores() {
        int lastIndex = Math.min(9, gameScoresSorted.size());
        return gameScoresSorted.subList(0, lastIndex);
    }
}
