package org.example.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.example.dao.MyBatisConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;


public class RandomNumberDAOTest extends EmbeddedMySql {

  @AfterEach
  public void delete() {
    RandomNumberDAO dao = new RandomNumberDAO(MyBatisConnectionFactory.getSqlSessionFactory());

    dao.masterDeleteAll();
  }

  @Test
  public void masterInsertTest() {
    int count = 1000;
    RandomNumberDAO dao = new RandomNumberDAO(MyBatisConnectionFactory.getSqlSessionFactory());

    for (int i = 0; i < count; i++) {
      dao.masterInsert(new RandomNumberRepo());
    }

    List<RandomNumberRepo> repoList = dao.masterSelectAll();

    assertEquals(count, repoList.size());
  }

  @Test
  public void selectByTimestampTest() {
    int count = 1000;
    RandomNumberDAO dao = new RandomNumberDAO(MyBatisConnectionFactory.getSqlSessionFactory());

    for (int i = 0; i < count; i++) {
      dao.masterInsert(new RandomNumberRepo());
    }

    List<RandomNumberRepo> repoListAll = dao.masterSelectAll();

    assertEquals(count, repoListAll.size());

    List<RandomNumberRepo> repoList = dao.masterSelectNumByTimestamp(1);

    assertTrue(repoList.size() > 0);
  }
}
