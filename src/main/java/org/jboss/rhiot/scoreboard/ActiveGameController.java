package org.jboss.rhiot.scoreboard;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import org.apache.log4j.Logger;

/**
 * Created by starksm on 6/19/16.
 */
public class ActiveGameController {
    private static final Logger log = Logger.getLogger(ActiveGameController.class);
    @FXML
    private Pane rootPane;
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
    private ObservableList<GameScore> gameScores = FXCollections.observableArrayList();
    private SortedList<GameScore> gameScoresSorted = new SortedList<>(gameScores, new GameScoreComparator());
    // In progress tag address to GameScore map
    private ConcurrentHashMap<String, GameScore> activeGameScores = new ConcurrentHashMap<>();

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        addressColumn.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        gatewayColumn.setCellValueFactory(cellData -> cellData.getValue().gatewayProperty().asObject());
        timeColumn.setCellValueFactory(cellData -> cellData.getValue().timeStringProperty());
        shotsColumn.setCellValueFactory(cellData -> cellData.getValue().shotsProperty().asObject());
        scoreColumn.setCellFactory((col) -> new FlashingTableCell());
        scoreColumn.setCellValueFactory(cellData -> cellData.getValue().scoreProperty().asObject());

        gameScoresSorted.comparatorProperty().bind(scoreTableView.comparatorProperty());
        scoreTableView.setItems(gameScoresSorted);
        //scoreTableView.setStyle("-fx-background-image: url('images/redhat.png')");
    }

    public Pane getRootPane() {
        return rootPane;
    }
    public void addGame(GameScore gs) {
        gameScores.add(gs);
    }

    public synchronized GameScore removeGame(String tagAddress) {
        GameScore gs = activeGameScores.remove(tagAddress);
        log.info(String.format("removeGame(%s), count=%d", tagAddress, activeGameScores.size()));
        if(!gameScores.remove(gs))
            log.warn("Failed to remove game: "+gs);
        return gs;
    }
    public int size() {
        return activeGameScores.size();
    }
    public void clear() {
        activeGameScores.clear();
        gameScores.clear();
    }

    public synchronized GameScore getGameScore(String tagAddress) {
        GameScore gs = activeGameScores.get(tagAddress);
        if(gs == null) {
            gs = new GameScore(tagAddress);
            activeGameScores.put(tagAddress, gs);
            gameScores.add(gs);
            log.info("Created game for: "+tagAddress);
            //gs.scoreProperty().bind();
        }
        return gs;
    }
}
