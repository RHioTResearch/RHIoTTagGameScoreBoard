package org.jboss.rhiot.scoreboard;

import java.util.Comparator;

/**
 * Created by starksm on 6/17/16.
 */
public class GameScoreComparator implements Comparator<GameScore> {
    @Override
    public int compare(GameScore score1, GameScore score2) {
        if( score1.getScore() < score2.getScore() ) {
            return -1;
        } else if( score1.getScore() > score2.getScore() ) {
            return 1;
        } else {
            return 0;
        }
    }
}
