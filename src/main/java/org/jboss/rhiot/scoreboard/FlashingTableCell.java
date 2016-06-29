package org.jboss.rhiot.scoreboard;

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
 * Created by starksm on 6/20/16.
 */
public class FlashingTableCell<S, T> extends TableCell<S, T> {
    private static final Duration HIGHLIGHT_TIME = Duration.millis(300);

    private final Pane fadePane = new Pane();
    private final FadeTransition animation;
    private final LabeledText labeledText = new LabeledText(this);
    private final StackPane container = new StackPane();
    private final Background background = new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY));

    public FlashingTableCell() {
        super();
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        fadePane.setBackground(background);
        fadePane.setOpacity(0);
        animation = new FadeTransition(HIGHLIGHT_TIME, fadePane);
        setPadding(Insets.EMPTY);
        container.getChildren().addAll(fadePane, labeledText);
        setGraphic(container);
    }


    @Override
    public void updateItem(T value, boolean empty) {
        super.updateItem(value, empty);
        if(empty) {
            labeledText.setText("");
            return;
        }

        labeledText.setText(value.toString());
        animation.setFromValue(1);
        animation.setToValue(0);
        animation.setCycleCount(1);
        animation.setAutoReverse(true);
        animation.playFromStart();
    }
}
