package org.jboss.rhiot.scoreboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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
    private static final String BROKER_URL   = "mqtt://broker-sandbox.everyware-cloud.com:1883";
    private static final String USERNAME     = "s-stark";
    //
    private static final String GAME_TOPIC = "+/org.jboss.rhiot.services.RHIoTTagScanner/gameScores";

    private EdcCloudClient edcCloudClient;
    /** Synchornization latch to allow client startup to wait for ack of topic subscriptions */
    private CountDownLatch subConfirmLatch;

    public void start() throws Exception {
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
        prof.setModelName("Java Client");

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
        subConfirmLatch = new CountDownLatch(2);
        log.info("Subscribe to data topics under: "+ GAME_TOPIC);
        int dataSubID = edcCloudClient.subscribe(GAME_TOPIC, "#", 1);

        System.out.println("Subscribe to control topics of all assets in the account");
        int controlSubID = edcCloudClient.controlSubscribe("+", "#", 1);

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
        log.info("publish:"+assetId+", topic="+topic);
        if(msg.metrics().containsKey(IRHIoTTagScanner.GW_LAST_GAME_SCORE)) {
            String name = (String) msg.getMetric(IRHIoTTagScanner.GW_LAST_GAME_TAG_NAME);
            int score = (int) msg.getMetric(IRHIoTTagScanner.GW_LAST_GAME_SCORE);
            int hits = (int) msg.getMetric(IRHIoTTagScanner.GW_LAST_GAME_SCORE_HITS);
            String tagAddress = (String) msg.getMetric(IRHIoTTagScanner.GW_LAST_GAME_SCORE_TAG_ADDRESS);
            Boolean isNewHighScore = (Boolean) msg.getMetric(IRHIoTTagScanner.GW_LAST_GAME_NEW_HIGH_SCORE);
            log.info(String.format("New game score: %s@%d, hits=%d, tag: %s, isNewHigh: %s@%s", name, score, hits, tagAddress, isNewHighScore, msg.getTimestamp()));
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
