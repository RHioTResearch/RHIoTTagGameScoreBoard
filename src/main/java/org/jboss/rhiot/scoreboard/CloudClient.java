package org.jboss.rhiot.scoreboard;

import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import com.eurotech.cloud.client.EdcCallbackHandler;
import com.eurotech.cloud.client.EdcClientException;
import com.eurotech.cloud.client.EdcClientFactory;
import com.eurotech.cloud.client.EdcCloudClient;
import com.eurotech.cloud.client.EdcConfiguration;
import com.eurotech.cloud.client.EdcConfigurationFactory;
import com.eurotech.cloud.client.EdcDeviceProfile;
import com.eurotech.cloud.client.EdcDeviceProfileFactory;
import com.eurotech.cloud.message.EdcPayload;
import org.apache.log4j.Logger;
import org.jboss.rhiot.services.api.IRHIoTTagScanner;

/**
 * Created by starksm on 6/13/16.
 */
public class CloudClient implements EdcCallbackHandler {
    private static final Logger log = Logger.getLogger(CloudClient.class);

    private static final String ACCOUNT_NAME = "Red-Hat";
    private static final String ASSET_ID     = "RHIoTTag-scoreboard";
    //private static final String BROKER_URL   = "mqtt://broker-Red-Hat.everyware-cloud.com:1883/";
    private static final String BROKER_URL   = "mqtt://broker-Red-Hat.everyware-cloud.com:1883/";
    private static final String USERNAME     = "s-stark";
    // subscript to all gameScore topics for all gateways
    private static final String GAME_SCORES_TOPIC = "+/org.jboss.rhiot.services.RHIoTTagScanner/gameScores";
    private static final String GAME_INFO_TOPIC = "+/org.jboss.rhiot.services.RHIoTTagScanner/gameInfo";

    private EdcCloudClient edcCloudClient;
    private ICloudListener listener;
    /** Synchronization latch to allow client startup to wait for ack of topic subscriptions */
    private CountDownLatch subConfirmLatch;

    public void start(ICloudListener listener) throws Exception {
        this.listener = listener;
        // Get the password
        String password = System.getenv("PASSWORD");
        if(password == null) {
            // Look to local properties file
            File sbpFile = new File("scoreboard.properties");
            if(sbpFile.canRead()) {
                FileReader cwdProps = new FileReader(sbpFile);
                Properties props = new Properties();
                props.load(cwdProps);
                password = props.getProperty("PASSWORD");
            }
            if(password == null || password.length() == 0)
                throw new IllegalStateException("Failed to get cloud password from PASSWORD env or scoreboard.properties");
        }

        //
        // Configure: create client configuration, and set its properties
        //
        EdcConfigurationFactory confFact = EdcConfigurationFactory.getInstance();
        EdcConfiguration conf = confFact.newEdcConfiguration(ACCOUNT_NAME,
                ASSET_ID,
                BROKER_URL,
                ASSET_ID,
                USERNAME,
                password);

        EdcDeviceProfileFactory profFactory = EdcDeviceProfileFactory.getInstance();
        EdcDeviceProfile prof = profFactory.newEdcDeviceProfile();
        prof.setDisplayName("RHIoTTag ScoreBoard");
        prof.setModelName("Java8 Client");

        //set GPS position in device profile - this is sent only once, with the birth certificate
        prof.setLongitude(-122.4194);
        prof.setLatitude(37.7749);

        //
        // Connect and start the session
        //
        edcCloudClient = EdcClientFactory.newInstance(conf, prof, this);
        edcCloudClient.startSession();
        log.info("Session started");

        //
        // Subscribe
        subConfirmLatch = new CountDownLatch(3);
        log.info("Subscribe to game scores under: "+ GAME_SCORES_TOPIC);
        int gameScoreID = edcCloudClient.subscribe(GAME_SCORES_TOPIC, "#", 1);
        log.info("Subscribe to game info under: "+ GAME_INFO_TOPIC);
        int gameInfoID = edcCloudClient.subscribe(GAME_INFO_TOPIC, "#", 1);

        System.out.println("Subscribe to control topics of all Gateway assets in the account");
        int controlSubID = edcCloudClient.controlSubscribe("DN2016GW+", "#", 1);

        // Wait until the subscriptions have been confirmed
        subConfirmLatch.await();
    }

    public void stop() throws EdcClientException {
        //
        // Stop the session and close the connection
        //
        edcCloudClient.stopSession();
        edcCloudClient.terminate();
        log.info("Terminating Cloud Client");
    }

    @Override
    public void controlArrived(String assetId, String topic, EdcPayload msg, int qos, boolean retain) {
        log.info("Control publish arrived on semantic topic: " + topic + " , qos: " + qos);
    }

    @Override
    public void publishArrived(String assetId, String topic, EdcPayload msg, int qos, boolean retain) {
        log.debug("publish:"+assetId+", topic="+topic);
        int gwIndex = assetId.indexOf("GW");
        String gwNo = assetId.substring(gwIndex+2);
        int gateway = Integer.parseInt(gwNo);

        // Game scores
        if(msg.metrics().containsKey(IRHIoTTagScanner.GW_LAST_GAME_SCORE)) {
            Date time = msg.getTimestamp();
            String name = (String) msg.getMetric(IRHIoTTagScanner.GW_LAST_GAME_TAG_NAME);
            int score = (int) msg.getMetric(IRHIoTTagScanner.GW_LAST_GAME_SCORE);
            int hits = (int) msg.getMetric(IRHIoTTagScanner.GW_LAST_GAME_SCORE_HITS);
            String tagAddress = (String) msg.getMetric(IRHIoTTagScanner.GW_LAST_GAME_SCORE_TAG_ADDRESS);
            boolean isNewHighScore = (boolean) msg.getMetric(IRHIoTTagScanner.GW_LAST_GAME_NEW_HIGH_SCORE);
            //log.info(String.format("New game score: %s@%d, hits=%d, tag: %s, isNewHigh: %s@%s", name, score, hits, tagAddress, isNewHighScore, msg.getTimestamp()));
            // int gateway, String name, String address, int shootingTimeLeft, int shotsLeft, int gameScore, int gameTimeLeft
            if(listener != null)
                listener.endGame(time, gateway, name, tagAddress, hits, score, isNewHighScore);
        }
        // In progress game info
        if(msg.metrics().containsKey(IRHIoTTagScanner.TAG_GAME_NAME)) {
            Date time = msg.getTimestamp();
            String name = (String) msg.getMetric(IRHIoTTagScanner.TAG_GAME_NAME);
            int score = (int) msg.getMetric(IRHIoTTagScanner.TAG_GAME_SCORE);
            int hits = (int) msg.getMetric(IRHIoTTagScanner.TAG_GAME_HITS);
            String tagAddress = (String) msg.getMetric(IRHIoTTagScanner.TAG_GAME_ADDRESS);
            int timeLeft = (int) msg.getMetric(IRHIoTTagScanner.TAG_GAME_TIME_LEFT);
            int shootingTimeLeft = (int) msg.getMetric(IRHIoTTagScanner.TAG_SHOOTING_TIME_LEFT);
            int shotsLeft = (int) msg.getMetric(IRHIoTTagScanner.TAG_SHOTS_LEFT);
            listener.gameInfo(time, gateway, name, tagAddress, hits, score, shotsLeft, shootingTimeLeft, timeLeft);
        }
    }

    public void connectionLost() {
        log.warn("EDC client connection lost");
    }

    public void connectionRestored() {
        log.warn("EDC client reconnected");
    }
    public void published(int messageId) {
        log.info("Publish message ID: " + messageId + " confirmed");
    }

    public void subscribed(int messageId) {
        subConfirmLatch.countDown();
        log.info("Subscribe message ID: " + messageId + " confirmed");
    }

    public void unsubscribed(int messageId) {
        log.info("Unsubscribe message ID: " + messageId + " confirmed");
    }

    public void controlArrived(String assetId, String topic, byte[] payload, int qos, boolean retain) {
        log.info("controlArrived, assetId: "+assetId+", topic: "+topic);
    }

    public void publishArrived(String assetId, String topic, byte[] payload, int qos, boolean retain) {
        log.debug("publishArrived, assetId: "+assetId+", topic: "+topic);
    }

}
