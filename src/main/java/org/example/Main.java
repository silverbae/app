package org.example;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.example.service.RandomNumberClone;
import org.example.service.RandomNumberGenerator;
import org.example.service.RandomNumberSync;

public class Main {

  private static final ScheduledExecutorService randomGenService = Executors.newSingleThreadScheduledExecutor();
  private static final ScheduledExecutorService randomSyncService = Executors.newSingleThreadScheduledExecutor();
  private static final ExecutorService randomCloneService = Executors.newSingleThreadExecutor();
  final static Logger log = Logger.getLogger("org.example");

  private static String getMode(String[] args) {
    if (args.length > 0 && args[0].equals("slave")) {
      return "slave";
    }

    return "master";
  }

  public static void main(String[] args) {

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("shutdown");

      randomGenService.shutdown();
      randomSyncService.shutdown();
      randomCloneService.shutdown();

    }));

    if (Objects.equals(getMode(args), "slave")) {
      log.info("start [slave]");

      RandomNumberClone randomNumberClone = new RandomNumberClone();
      randomCloneService.submit(randomNumberClone);

    } else {
      log.info("start [master]");

      RandomNumberGenerator randomNumberGenerator = new RandomNumberGenerator();
      randomGenService.scheduleAtFixedRate(randomNumberGenerator, 0, 100, TimeUnit.MILLISECONDS);

      RandomNumberSync randomNumberSync = new RandomNumberSync();
      randomSyncService.scheduleAtFixedRate(randomNumberSync, 0, 1, TimeUnit.SECONDS);
    }
  }
}
