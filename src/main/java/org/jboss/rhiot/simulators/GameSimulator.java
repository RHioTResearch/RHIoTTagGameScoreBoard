package org.jboss.rhiot.simulators;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.jboss.rhiot.services.fsm.GameStateMachine;

/**
 * Created by starksm on 6/18/16.
 */
public class GameSimulator  {
    private static final Logger log = Logger.getLogger(GameSimulator.class);
    private static final long SLEEP_TIME = 5000;
    private static final String REST_BASE = "http://192.168.1.104:8080/rhiot/";
    private String restURLBase = REST_BASE;

    public static void main(String[] args) throws Exception {
        String restBase = REST_BASE;
        String[] tags = {"F0:B4:48:D6:DA:85", "F1:B4:48:D6:DA:85", "F2:B4:48:D6:DA:85", "F3:B4:48:D6:DA:85",
                "F4:B4:48:D6:DA:85", "F5:B4:48:D6:DA:85", "F6:B4:48:D6:DA:85", "F7:B4:48:D6:DA:85"
        };
        for(int n = 0; n < args.length; n ++) {
            if(args[n].startsWith("-url"))
                restBase = args[++n];
            if(args[n].startsWith("-tags")) {
                tags = args[++n].split(",");
            }
        }
        GameSimulator simulator = new GameSimulator(restBase);
        simulator.runSimulation(tags, null);
        System.out.printf("Simulation complete\n");
        System.exit(0);
    }

    public GameSimulator(String restURLBase) {
        this.restURLBase = restURLBase;
    }
    public GameSimulator() {
    }

    public void runSimulation(String[] tags, ScannerSimulator scanner) throws InterruptedException {
        log.info(String.format("Start runSimulation, tags=%s, scanner=%s", Arrays.asList(tags), scanner));
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        for(String tag : tags){
            if(scanner == null)
                executorService.submit(() -> sendTagData(tag));
            else
                executorService.submit(() -> sendTagData(tag, scanner));
        }
        executorService.awaitTermination(6, TimeUnit.MINUTES);
    }

    private void sendTagData(String tagAddress) {
        Client client = ClientBuilder.newClient();
        int keys = 0;
        int lux = 0;
        GameStateMachine.GameState state = GameStateMachine.GameState.IDLE;
        // Get the current state
        String stateName = client.target(restURLBase + "gamesm").queryParam("address", tagAddress).request().get().readEntity(String.class);
        state = GameStateMachine.GameState.valueOf(stateName);
        System.out.printf("Starting(%s) at: %s\n", tagAddress, state);
        do {
            // Generate key and lux data
            if(state == GameStateMachine.GameState.GAMEOVER || state == GameStateMachine.GameState.IDLE) {
                keys = 0b011;
                System.out.printf("Restarting(%s)\n", tagAddress);
            }
            else if(state == GameStateMachine.GameState.GUN_EMPTY) {
                keys = 0b010;
                System.out.printf("Reloading(%s)\n", tagAddress);
            }
            else if(state == GameStateMachine.GameState.REPLACE_TARGET) {
                keys = 0b01;
                System.out.printf("ReplaceTarget(%s)\n", tagAddress);
            }
            else if(state == GameStateMachine.GameState.RESETTING) {
                System.out.printf("Resetting(%s)\n", tagAddress);
                lux = 1000;
            }
            else
                keys = 0;
            if(state != GameStateMachine.GameState.RESETTING)
                lux = (int) (40000 * Math.random());
            String data = String.format("{address=\"%s\", keys=%d, lux=%d}", tagAddress, keys, lux);
            System.out.println(data);
            Response response = client.target(restURLBase + "inject-tag-data")
                    .request()
                    .put(Entity.entity(data, MediaType.APPLICATION_JSON_TYPE));
            stateName = response.readEntity(String.class);
            state = GameStateMachine.GameState.valueOf(stateName);
            System.out.printf("New State: %s\n", stateName);
            try {
                long sleepTime = (long) Math.rint(100 + Math.random() * SLEEP_TIME);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(state != GameStateMachine.GameState.GAMEOVER);
        System.out.printf("GAMEOVER(%s)\n", tagAddress);
    }

    private void sendTagData(String tagAddress, ScannerSimulator scanner) {
        int keys = 0;
        int lux = 0;
        GameStateMachine.GameState state = GameStateMachine.GameState.IDLE;
        // Get the current state
        String stateName = scanner.getGameState(tagAddress);
        state = GameStateMachine.GameState.valueOf(stateName);
        System.out.printf("Starting(%s) at: %s\n", tagAddress, state);
        do {
            // Generate key and lux data
            if(state == GameStateMachine.GameState.GAMEOVER || state == GameStateMachine.GameState.IDLE) {
                keys = 0b011;
                System.out.printf("Restarting(%s)\n", tagAddress);
            }
            else if(state == GameStateMachine.GameState.GUN_EMPTY) {
                keys = 0b010;
                System.out.printf("Reloading(%s)\n", tagAddress);
            }
            else if(state == GameStateMachine.GameState.REPLACE_TARGET) {
                keys = 0b01;
                System.out.printf("ReplaceTarget(%s)\n", tagAddress);
            }
            else if(state == GameStateMachine.GameState.RESETTING) {
                System.out.printf("Resetting(%s)\n", tagAddress);
                lux = 1000;
            }
            else
                keys = 0;
            if(state != GameStateMachine.GameState.RESETTING)
                lux = (int) (40000 * Math.random());
            stateName = scanner.handleTagData(tagAddress, keys, lux);
            state = GameStateMachine.GameState.valueOf(stateName);
            System.out.printf("New State: %s\n", stateName);
            try {
                long sleepTime = (long) Math.rint(100 + Math.random() * SLEEP_TIME);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(state != GameStateMachine.GameState.GAMEOVER);
        System.out.printf("GAMEOVER(%s)\n", tagAddress);
    }

}
