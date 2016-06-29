package org.jboss.rhiot.scoreboard;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/**
 * Created by starksm on 6/22/16.
 */
public class TopScoresController {
    @FXML
    private GridPane gridPane;

    private List<TopScoreController> scoreControllers = new ArrayList<>();

    @FXML
    private void initialize() throws IOException {
        URL fxml = TopScoreController.class.getResource("topscore.fxml");
        for(int row = 0; row < 3; row ++) {
            for(int col = 0; col < 3; col ++) {
                FXMLLoader loader = new FXMLLoader(fxml);
                Pane scorePane = loader.load();
                TopScoreController controller = loader.getController();
                scoreControllers.add(controller);
                gridPane.add(scorePane, col, row);
                int rank = row*3+col;
                String name = "TBD#"+rank;
                String address = "00:01:02:03:04:0"+rank;
                controller.setTopScore(name, address, 0, rank);
            }
        }
    }

    public synchronized void updateTopScores(List<GameScore> scores) {
        int last = Math.min(scores.size(), scoreControllers.size());
        for (int rank = 0; rank < last; rank ++) {
            GameScore gs = scores.get(rank);
            TopScoreController tsc = scoreControllers.get(rank);
            tsc.updateScore(gs.getName(), gs.getAddress(), gs.getScore());
        }
    }
    public void updateTopScore(String name, String address, int score, int rank) {
        TopScoreController tsc = scoreControllers.get(rank);
        tsc.updateScore(name, address, score);
    }

    public GridPane getGridPane() {
        return gridPane;
    }
}
