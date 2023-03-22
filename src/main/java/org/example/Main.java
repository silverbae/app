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

    if (Objects.equals(getMode(args), "slave")) {
      log.info("start [slave]");

      RandomNumberClone randomNumberClone = new RandomNumberClone();
      randomCloneService.submit(randomNumberClone);

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        // RandomCloneService 종료 시도
        randomCloneService.shutdown();

        // 종료되지 않았다면 최대 5초 기다린 후 강제 종료
        try {
          if (!randomCloneService.awaitTermination(5, TimeUnit.SECONDS)) {
            log.warn("RandomCloneService is not terminated");
            randomCloneService.shutdownNow();
          }
        } catch (InterruptedException e) {
          log.error("error : " + e.getMessage());
        }
      }));
    } else {
      log.info("start [master]");

      RandomNumberGenerator randomNumberGenerator = new RandomNumberGenerator();
      randomGenService.scheduleAtFixedRate(randomNumberGenerator, 0, 100, TimeUnit.MILLISECONDS);

      RandomNumberSync randomNumberSync = new RandomNumberSync();
      randomSyncService.scheduleAtFixedRate(randomNumberSync, 0, 1, TimeUnit.SECONDS);

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        // RandomGenService, RandomSyncService 종료 시도
        randomGenService.shutdown();
        randomSyncService.shutdown();

        //  종료되지 않았다면 최대 5초 기다린 후 강제 종료
        try {
          if (!randomGenService.awaitTermination(5, TimeUnit.SECONDS)) {
            log.warn("RandomGenService is not terminated");
            randomGenService.shutdownNow();
          }

          if (!randomSyncService.awaitTermination(5, TimeUnit.SECONDS)) {
            log.warn("RandomSyncService is not terminated");
            randomSyncService.shutdownNow();
          }
        } catch (InterruptedException e) {
          log.error("error : " + e.getMessage());
        }
      }));
    }
  }
}
