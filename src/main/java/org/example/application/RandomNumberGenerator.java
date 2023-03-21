package org.example.application;

import org.example.MyBatisConnectionFactory;
import org.example.model.RandomNumberRepo;
import org.example.persistence.RandomNumberDAO;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 랜덤넘버 생성기 - 0.1초 주기로 양의 정수를 생성하여 db에 저장 한다.
 */
public class RandomNumberGenerator {

  private RandomNumberGenerator() {
  }

  public static RandomNumberGenerator getInstance() {
    return LazyHolder.instance;
  }

  private static class LazyHolder {

    private static final RandomNumberGenerator instance = new RandomNumberGenerator();
  }

  private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

  private void _start() {
    RandomNumberDAO dao = new RandomNumberDAO(MyBatisConnectionFactory.getSqlSessionFactory());

    service.scheduleAtFixedRate(() ->
        dao.masterInsert(new RandomNumberRepo()), 0, 100, TimeUnit.MILLISECONDS);
  }

  private void _stop() {
    service.shutdown();
  }

  public static void start() {
    getInstance()._start();
  }

  public static void stop() {
    getInstance()._stop();
  }

}
