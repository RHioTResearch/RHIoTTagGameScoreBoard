package org.jboss.rhiot.scoreboard;


import java.util.Date;

public interface ICloudListener {
    void endGame(Date time, int gateway, String name, String tagAddress, int hits, int score, boolean isNewHighScore);

    void gameInfo(Date time, int gateway, String name, String tagAddress, int hits, int score, int shotsLeft, int shootingTimeLeft, int timeLeft);
}
