package org.jboss.rhiot.scoreboard;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "GameScores")
public class GameScore implements Serializable, Comparable<GameScore> {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private Long id;
    private SimpleStringProperty name = new SimpleStringProperty();
    private SimpleStringProperty address = new SimpleStringProperty();
    private SimpleIntegerProperty gateway = new SimpleIntegerProperty();
    private SimpleObjectProperty<Date> time = new SimpleObjectProperty<>();
    private SimpleStringProperty timeString = new SimpleStringProperty();
    private SimpleIntegerProperty shots = new SimpleIntegerProperty();
    private SimpleIntegerProperty score = new SimpleIntegerProperty();
    private SimpleIntegerProperty timeLeft = new SimpleIntegerProperty();

    public GameScore(){this("");}
    public GameScore(String address) {
        this.name = new SimpleStringProperty("");
        this.address = new SimpleStringProperty(address);
        this.gateway = new SimpleIntegerProperty(0);
        setTime(new Date());
        this.shots = new SimpleIntegerProperty(0);
        this.score = new SimpleIntegerProperty(0);
    }
    public GameScore(String name, String address, Integer gateway, Date time, Integer shots, Integer score) {
        this.name = new SimpleStringProperty(name);
        this.address = new SimpleStringProperty(address);
        this.gateway = new SimpleIntegerProperty(gateway);
        setTime(time);
        this.shots = new SimpleIntegerProperty(shots);
        this.score = new SimpleIntegerProperty(score);
    }

    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    public Long getId() {
        return id;
    }
    private void setId(Long id) {
        this.id = id;
    }

    @Column(name = "TAGNAME", nullable = false, length = 64)
    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @Column(name = "TAGADDR", nullable = false, length = 20)
    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    @Column(name = "TAGGW", nullable = false)
    public int getGateway() {
        return gateway.get();
    }

    public SimpleIntegerProperty gatewayProperty() {
        return gateway;
    }

    public void setGateway(int gateway) {
        this.gateway.set(gateway);
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "GAME_DATE", nullable = false)
    public Date getTime() {
        return time.get();
    }

    public SimpleObjectProperty<Date> timeProperty() {
        return time;
    }

    public void setTime(Date time) {
        this.time.set(time);
        Instant instant = time.toInstant();
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        String ts = fmt.format(ldt);
        timeStringProperty().set(ts);
    }

    @Column(name = "GAME_SHOTS", nullable = false)
    public int getShots() {
        return shots.get();
    }

    public SimpleIntegerProperty shotsProperty() {
        return shots;
    }

    public void setShots(int shots) {
        this.shots.set(shots);
    }

    @Column(name = "GAME_SCORE", nullable = false)
    public int getScore() {
        return score.get();
    }

    public SimpleIntegerProperty scoreProperty() {
        return score;
    }

    public void setScore(int score) {
        this.score.set(score);
    }

    @Transient
    public int getTimeLeft() {
        return timeLeft.get();
    }

    public SimpleIntegerProperty timeLeftProperty() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft.set(timeLeft);
    }

    public String getTimeString() {
        return timeString.get();
    }

    public SimpleStringProperty timeStringProperty() {
        return timeString;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(GameScore o) {
        return getScore() - o.getScore();
    }

    @Override
    public String toString() {
        return "GameScore{" +
                "id=" + id +
                ", name=" + name +
                ", address=" + address +
                ", gateway=" + gateway +
                ", time=" + time +
                ", shots=" + shots +
                ", score=" + score +
                '}';
    }
}

