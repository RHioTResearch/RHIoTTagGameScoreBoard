package org.jboss.rhiot.scoreboard;

import java.util.Date;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by starksm on 6/17/16.
 */
public class GameScore {
    private SimpleStringProperty name;
    private SimpleStringProperty address;
    private SimpleIntegerProperty gateway;
    private SimpleObjectProperty<Date> time;
    private SimpleIntegerProperty shots;
    private SimpleIntegerProperty score;

    public GameScore(String name, String address, Integer gateway, Date time, Integer shots, Integer score) {
        this.name = new SimpleStringProperty(name);
        this.address = new SimpleStringProperty(address);
        this.gateway = new SimpleIntegerProperty(gateway);
        this.time = new SimpleObjectProperty<>(time);
        this.shots = new SimpleIntegerProperty(shots);
        this.score = new SimpleIntegerProperty(score);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public int getGateway() {
        return gateway.get();
    }

    public SimpleIntegerProperty gatewayProperty() {
        return gateway;
    }

    public Date getTime() {
        return time.get();
    }

    public SimpleObjectProperty<Date> timeProperty() {
        return time;
    }

    public int getShots() {
        return shots.get();
    }

    public SimpleIntegerProperty shotsProperty() {
        return shots;
    }

    public int getScore() {
        return score.get();
    }

    public SimpleIntegerProperty scoreProperty() {
        return score;
    }
}

