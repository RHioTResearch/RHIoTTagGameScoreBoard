package org.jboss.rhiot.scoreboard;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by starksm on 6/22/16.
 */
public class TopScoreController {
    @FXML
    private Label nameLabel;
    @FXML
    private Label addrLabel;
    @FXML
    private Label scoreLabel;
    @FXML
    private ImageView bannerView;
    @FXML
    private Label rankLabel;

    @FXML
    private void initialize() {
    }

    public void setTopScore(String name, String address, int score, int rank) {
        Platform.runLater(() -> {
            rankLabel.setText(""+rank);
            nameLabel.setText(name);
            addrLabel.setText(address);
            scoreLabel.setText(String.format("%,d", score));
            loadBanner(rank);
        });
    }
    public void updateScore(String name, String address, int score) {
        Platform.runLater(() -> {
            nameLabel.setText(name);
            addrLabel.setText(address);
            scoreLabel.setText(String.format("%,d", score));
        });
    }
    private void loadBanner(int rank) {
        String imageName = "/images/banner_steel.png";
        switch (rank) {
            case 0:
                imageName = "/images/banner_gold.png";
                break;
            case 1:
                imageName = "/images/banner_silver.png";
                break;
            case 2:
                imageName = "/images/banner_bronze.png";
                break;
        }
        bannerView.setImage(new Image(imageName));
    }
}
