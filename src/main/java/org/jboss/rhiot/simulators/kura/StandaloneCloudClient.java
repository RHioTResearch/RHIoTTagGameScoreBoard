package org.jboss.rhiot.simulators.kura;

import java.util.List;

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
import org.eclipse.kura.KuraErrorCode;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudClientListener;
import org.eclipse.kura.message.KuraPayload;

/**
 * Created by starksm on 6/20/16.
 */
public class StandaloneCloudClient implements CloudClient, EdcCallbackHandler {
    private static final Logger log = Logger.getLogger(StandaloneCloudClient.class);
    private static final String ACCOUNT_NAME = "Red-Hat";
    private static final String ASSET_ID     = "DN2016-GW14";
//    private static final String BROKER_URL   = "mqtt://broker-sandbox.everyware-cloud.com:1883";
    private static final String BROKER_URL   = "mqtt://broker-Red-Hat.everyware-cloud.com:1883/";
    private static final String USERNAME     = "s-stark";

    private EdcCloudClient edcCloudClient;
    private String appID;
    private String assetID = ASSET_ID;
    private CloudClientListener listener;

    StandaloneCloudClient(String appID, String assetID) {
        this.appID = appID;
        this.assetID = assetID;
    }

    public void start() throws Exception {
        String password = System.getenv("PASSWORD");
        EdcConfigurationFactory confFact = EdcConfigurationFactory.getInstance();
        EdcConfiguration conf = confFact.newEdcConfiguration(ACCOUNT_NAME,
                assetID,
                BROKER_URL,
                assetID,
                USERNAME,
                password);

        EdcDeviceProfileFactory profFactory = EdcDeviceProfileFactory.getInstance();
        EdcDeviceProfile prof = profFactory.newEdcDeviceProfile();
        prof.setDisplayName(assetID+" Simulator");
        prof.setModelName("Java8 Client");

        edcCloudClient = EdcClientFactory.newInstance(conf, prof, this);
        edcCloudClient.startSession();
        log.info("Session started");

        log.info("Subscribe to control topics of all Gateway assets in the account");
        for(int n = 0; n < 15; n ++) {
            int controlSubID = edcCloudClient.controlSubscribe("DN2016-GW"+n, "#", 1);
        }

    }

    @Override
    public String getApplicationId() {
        return appID;
    }

    @Override
    public void release() {
        try {
            edcCloudClient.stopSession();
            edcCloudClient.terminate();
        } catch (EdcClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public int publish(String topic, KuraPayload kuraPayload, int qos, boolean retain) throws KuraException {
        EdcPayload payload = new EdcPayload();
        payload.setTimestamp(kuraPayload.getTimestamp());
        kuraPayload.metrics().forEach(payload::addMetric);
        try {
            String fullTopic = appID + "/" + topic;
            edcCloudClient.publish(fullTopic, payload, qos, retain);
        } catch (EdcClientException e) {
            throw new KuraException(KuraErrorCode.INTERNAL_ERROR, e);
        }
        return 0;
    }

    @Override
    public int publish(String s, KuraPayload kuraPayload, int i, boolean b, int i1) throws KuraException {
        return 0;
    }

    @Override
    public int publish(String s, byte[] bytes, int i, boolean b, int i1) throws KuraException {
        return 0;
    }

    @Override
    public int controlPublish(String s, KuraPayload kuraPayload, int i, boolean b, int i1) throws KuraException {
        return 0;
    }

    @Override
    public int controlPublish(String s, String s1, KuraPayload kuraPayload, int i, boolean b, int i1) throws KuraException {
        return 0;
    }

    @Override
    public int controlPublish(String s, String s1, byte[] bytes, int i, boolean b, int i1) throws KuraException {
        return 0;
    }

    @Override
    public void subscribe(String s, int i) throws KuraException {

    }

    @Override
    public void controlSubscribe(String s, int i) throws KuraException {

    }

    @Override
    public void unsubscribe(String s) throws KuraException {

    }

    @Override
    public void controlUnsubscribe(String s) throws KuraException {

    }

    @Override
    public void addCloudClientListener(CloudClientListener cloudClientListener) {
        listener = cloudClientListener;
    }

    @Override
    public void removeCloudClientListener(CloudClientListener cloudClientListener) {
        listener = null;
    }

    @Override
    public List<Integer> getUnpublishedMessageIds() throws KuraException {
        return null;
    }

    @Override
    public List<Integer> getInFlightMessageIds() throws KuraException {
        return null;
    }

    @Override
    public List<Integer> getDroppedInFlightMessageIds() throws KuraException {
        return null;
    }

// EdcCallbackHandler
    @Override
    public void controlArrived(String s, String s1, EdcPayload edcPayload, int i, boolean b) {
        if(listener != null) {
            KuraPayload payload = new KuraPayload();
            payload.setTimestamp(edcPayload.getTimestamp());
            edcPayload.metrics().forEach(payload::addMetric);
            listener.onControlMessageArrived(s, s1, payload, i, b);
        }
    }

    @Override
    public void publishArrived(String s, String s1, EdcPayload edcPayload, int i, boolean b) {
        if(listener != null) {
        }
    }

    @Override
    public void controlArrived(String s, String s1, byte[] bytes, int i, boolean b) {

    }

    @Override
    public void publishArrived(String s, String s1, byte[] bytes, int i, boolean b) {

    }

    @Override
    public void connectionLost() {
        if(listener != null)
            listener.onConnectionLost();
    }

    @Override
    public void connectionRestored() {
        if(listener != null)
            listener.onConnectionEstablished();
    }

    @Override
    public void published(int i) {
    }

    @Override
    public void subscribed(int i) {
    }

    @Override
    public void unsubscribed(int i) {
    }
}
