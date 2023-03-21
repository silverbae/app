package org.example.application;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.example.MyBatisConnectionFactory;
import org.example.model.EmbeddedMySql;
import org.example.model.RandomNumberRepo;
import org.example.persistence.RandomNumberDAO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomNumberGeneratorTest extends EmbeddedMySql {

  @BeforeAll
  public static void start() {
    RandomNumberGenerator.start();
  }

  @AfterAll
  public static void stop() {
    RandomNumberGenerator.stop();
  }

  @Test
  public void genTest() throws InterruptedException {
    RandomNumberDAO dao = new RandomNumberDAO(MyBatisConnectionFactory.getSqlSessionFactory());

    Thread.sleep(10000);

    List<RandomNumberRepo> repoList = dao.masterSelectAll();

    assertTrue(repoList.size() > 0);
  }

}