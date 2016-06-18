package org.jboss.rhiot.scoreboard;

/**
 * Created by starksm on 6/13/16.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        CloudClient cloudClient = new CloudClient();
        cloudClient.start();

    }
}
