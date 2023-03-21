package org.example;

import java.util.Objects;
import org.apache.log4j.Logger;
import org.example.application.RandomNumberClone;
import org.example.application.RandomNumberGenerator;
import org.example.application.RandomNumberSync;

public class Main {

    final static Logger log = Logger.getLogger("common");

    private static String getMode(String[] args) {
        if (args.length > 0 && args[0].equals("slave")) {
            return "slave";
        }

        return "master";
    }

    public static void main(String[] args) throws InterruptedException {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("shutdown");

            RandomNumberGenerator.stop();
            RandomNumberSync.stop();
            RandomNumberClone.stop();
        }));

        if (Objects.equals(getMode(args), "slave")) {
            log.info("start [slave]");

            RandomNumberClone.start();
        } else {
            log.info("start [master]");

            RandomNumberGenerator.start();
            Thread.sleep(1000);
            RandomNumberSync.start();
        }
    }
}