package org.jboss.rhiot.scoreboard;

import java.util.Comparator;

import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Created by starksm on 6/17/16.
 */
public class GameScoreComparator implements Comparator<GameScore> {
    /**
     * sorts from high to low
     * @param score1
     * @param score2
     * @return
     */
    @Override
    public int compare(GameScore score1, GameScore score2) {
        return score2.getScore() - score1.getScore();
    }

}
