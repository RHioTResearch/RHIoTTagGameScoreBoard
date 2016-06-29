package org.jboss.rhiot.simulators;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.eurotech.cloud.client.EdcClientException;
import org.jboss.rhiot.ble.bluez.RHIoTTag;
import org.jboss.rhiot.services.fsm.GameStateMachine;
import org.jboss.rhiot.simulators.kura.MockHttpService;
import org.jboss.rhiot.simulators.kura.StandaloneCloudService;
import org.jboss.rhiot.simulators.kura.UndertowHttpService;
import org.apache.log4j.Logger;
import org.jboss.rhiot.services.RHIoTTagScanner;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;

/**
 * Created by starksm on 6/20/16.
 */
public class ScannerSimulator {
    private static final Logger log = Logger.getLogger(ScannerSimulator.class);
    private StandaloneCloudService cloudService;
    private RHIoTTagScanner scanner;
    private HttpService httpService;
    private volatile boolean running;

    public static void main(String[] args) throws Exception {
        Map<String, Object> properties = null;
        if(args.length > 0) {
            // Name of Map<String,Object> properties
            FileInputStream fis = new FileInputStream(args[0]);
            ObjectInputStream ois = new ObjectInputStream(fis);
            properties = (Map<String, Object>) ois.readObject();
            ois.close();
            System.out.printf("Loaded properties from: %s\n", args[0]);
        } else {
            System.out.printf("Running with default properties\n");
        }
        ScannerSimulator simulator = new ScannerSimulator();
        simulator.start(properties);
    }

    private void start(Map<String, Object> properties) throws Exception {
        Class<?> parameterTypes[] = {ComponentContext.class, Map.class};
        Method activate = RHIoTTagScanner.class.getDeclaredMethod("activate", parameterTypes);
        activate.setAccessible(true);
        if(properties == null) {
            properties = new HashMap<>();
            properties.put("publish.semanticTopic", "data");
            properties.put("publish.retain", Boolean.FALSE);
            properties.put("publish.qos", 0);
            properties.put("hciDev", "hci0");
            properties.put("cloud.password", "any");
            properties.put("game.duration", 300);
            properties.put("game.shootingWindow", 45);
            properties.put("skipJniInitialization", Boolean.TRUE);
            String[] tag0Info = {"F0:B4:48:D6:DA:85", "GW-14:TestTag0"};
            properties.put("gw.tag0", tag0Info);
            String[] tag1Info = {"F1:B4:48:D6:DA:85", "GW-14:TestTag1"};
            properties.put("gw.tag1", tag1Info);
            String[] tag2Info = {"F2:B4:48:D6:DA:85", "GW-14:TestTag2"};
            properties.put("gw.tag2", tag2Info);
            String[] tag3Info = {"F3:B4:48:D6:DA:85", "GW-14:TestTag3"};
            properties.put("gw.tag3", tag3Info);
            String[] tag4Info = {"F4:B4:48:D6:DA:85", "GW-14:TestTag4"};
            properties.put("gw.tag4", tag4Info);
            String[] tag5Info = {"F5:B4:48:D6:DA:85", "GW-14:TestTag5"};
            properties.put("gw.tag5", tag5Info);
            String[] tag6Info = {"F6:B4:48:D6:DA:85", "GW-14:TestTag6"};
            properties.put("gw.tag6", tag6Info);
            String[] tag7Info = {"F7:B4:48:D6:DA:85", "GW-14:TestTag7"};
            properties.put("gw.tag7", tag7Info);
        }

        String gwID = (String) properties.get("gwID");
        System.out.printf("Using gwID: %s\n", gwID);
        if(gwID != null) {
            cloudService = new StandaloneCloudService(gwID);
        } else {
            cloudService = new StandaloneCloudService();
        }
        Boolean mockHttpService = (Boolean) properties.get("mockHttpService");
        System.out.printf("mockHttpService: %s\n", mockHttpService);
        if(mockHttpService == Boolean.TRUE) {
            httpService = new MockHttpService();
            System.out.printf("Using MockHttpService\n");
        } else {
            httpService = new UndertowHttpService();
            System.out.printf("Using UndertowHttpService\n");
        }
        scanner = new RHIoTTagScanner();
        scanner.setCloudService(cloudService);
        scanner.setHttpService(httpService);
        activate.invoke(scanner, null, properties);

        running = true;
        generateTagData(properties);
    }

    public void stop() throws EdcClientException {
        log.info("Terminating Cloud Client");
    }

    private void generateTagData(Map<String, Object> properties) throws InterruptedException {
        ArrayList<String> tags = new ArrayList<>();
        for(int n = 0; n < 8; n ++) {
            String[] tagInfo = (String[]) properties.get("gw.tag"+n);
            if(tagInfo != null)
                tags.add(tagInfo[0]);
        }
        System.out.printf("Starting game simulators for tags: %s\n", tags);
        GameSimulator gameSimulator = new GameSimulator();
        String[] addresses = new String[tags.size()];
        tags.toArray(addresses);
        gameSimulator.runSimulation(addresses, this);
    }

    public String getGameState(String tagAddress) {
        String state = scanner.getAndPublishGameSMInfo(tagAddress);
        return state;
    }

    public String handleTagData(String tagAddress, int keys, int lux) {
        String name = scanner.getTagInfo(tagAddress);
        RHIoTTag tag = new RHIoTTag(tagAddress, (byte) keys, lux);
        tag.setName(name+"Sim");
        CompletableFuture<GameStateMachine.GameState> future = scanner.handleTagAsync(tag);
        GameStateMachine.GameState state = GameStateMachine.GameState.IDLE;
        try {
            state = future.get();
        } catch (Exception e) {
            log.error("Failed to handle put for tag: "+tagAddress, e);
        }
        return state.name();
    }
}
