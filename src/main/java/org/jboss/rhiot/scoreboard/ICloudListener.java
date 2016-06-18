package org.jboss.rhiot.scoreboard;


public interface ICloudListener {
    public void gameInfo(int gateway, String name, String address, int shootingTimeLeft, int shotsLeft, int gameScore, int gameTimeLeft);
    public void hitDetected(int hitScore, int ringsOffCenter);

}
