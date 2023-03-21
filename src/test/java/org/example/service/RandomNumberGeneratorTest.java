package org.example.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.example.MyBatisConnectionFactory;
import org.example.mapper.EmbeddedMySql;
import org.example.mapper.RandomNumberDAO;
import org.example.mapper.RandomNumberRepo;

class RandomNumberGeneratorTest extends EmbeddedMySql {

  @BeforeAll
  public static void start() {
    RandomNumberGenerator randomNumberGenerator = new RandomNumberGenerator();

    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    service.scheduleAtFixedRate(randomNumberGenerator, 0, 100, TimeUnit.MILLISECONDS);
  }

  @AfterAll
  public static void stop() {
  }

  @Test
  public void genTest() throws InterruptedException {

    Thread.sleep(10000);

    RandomNumberDAO dao = new RandomNumberDAO(MyBatisConnectionFactory.getSqlSessionFactory());
    List<RandomNumberRepo> repoList = dao.masterSelectAll();

    assertTrue(repoList.size() > 0);
  }

}