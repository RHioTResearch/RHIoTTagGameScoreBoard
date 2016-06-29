package org.jboss.rhiot.simulators;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by starksm on 6/22/16.
 */
public class ScannerSimulatorDriver {
    public static void main(String[] args) throws Exception {
        String tagBase = "F0:B4:48:D6:DA:";
        System.out.printf("Generating 14 ScannerSimulator processes...\n");

        File fatJar = new File("target/scanner-driver-fat.jar");
        ExecutorService executorService = Executors.newFixedThreadPool(14);
        for(int gw = 1; gw < 15; gw ++) {
            String gwTagBase = tagBase + Integer.toHexString(gw).toUpperCase();
            File runDir = generateProperties(gwTagBase, gw);
            File logFile = new File(runDir, "gw.log");
            File propFile = new File(runDir, "gw.props");
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", fatJar.getAbsolutePath(), propFile.getAbsolutePath());
            pb.directory(runDir);
            System.out.printf("GW%d, runDir=%s, log=%s\n", gw, runDir, logFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            pb.redirectOutput(logFile);
            final int gwID = gw;
            executorService.submit(() -> runSimulator(gwID, pb));
        }
        executorService.awaitTermination(6, TimeUnit.MINUTES);
    }

    static void runSimulator(int gw, ProcessBuilder pb) {
        /*
        String[] args = {pb.directory().getAbsolutePath()+"/gw.props"};
        try {
            ScannerSimulator.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        try {
            List<String> command = pb.command();
            System.out.printf("Starting GW%d process: %s\n", gw, command);
            Process process = pb.start();
            System.out.printf("Started GW%d process: %s\n", gw, process);
            int exitCode = process.waitFor();
            System.out.printf("DN2016-GW%d exited with: %d\n", gw, exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static File generateProperties(String tagBase, int gw) throws IOException {
        HashMap<String,Object> properties = new HashMap<>();

        properties.put("gwID", "DN2016-GW"+gw);
        properties.put("mockHttpService", Boolean.TRUE);
        properties.put("publish.semanticTopic", "data");
        properties.put("publish.retain", Boolean.FALSE);
        properties.put("publish.qos", 0);
        properties.put("hciDev", "hci0");
        properties.put("cloud.password", "any");
        properties.put("game.duration", 300);
        properties.put("game.shootingWindow", 45);
        properties.put("skipJniInitialization", Boolean.TRUE);
        for(int tag = 0; tag < 8; tag ++) {
            String[] tagInfo = {tagBase+tag, "GW-"+gw+":TestTag"+tag};
            properties.put("gw.tag"+tag, tagInfo);
        }
        File runDir = new File("/tmp/scanners/gw"+gw);
        runDir.mkdirs();
        File propsFile = new File(runDir, "gw.props");
        FileOutputStream fos = new FileOutputStream(propsFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(properties);
        oos.close();
        fos.close();
        return runDir;
    }
}
