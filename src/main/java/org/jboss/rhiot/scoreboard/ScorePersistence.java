package org.jboss.rhiot.scoreboard;

import java.util.Date;

/**
 * ICloudListener that listens for gameScore messages to save them for the scoreboard app
 */
public class ScorePersistence implements ICloudListener {

    public static void main(String[] args) {

    }

    @Override
    public void endGame(Date time, int gateway, String name, String tagAddress, int hits, int score, boolean isNewHighScore) {

    }

    @Override
    public void gameInfo(Date time, int gateway, String name, String tagAddress, int hits, int score, int shotsLeft, int shootingTimeLeft, int timeLeft) {

    }
}
