package org.example.mapper;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class RandomNumberDAO {

  private final SqlSessionFactory sqlSessionFactory;

  public RandomNumberDAO(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
  }

  /**
   * for master
   **/
  public void masterInsert(RandomNumberRepo randomNumberRepo) {

    SqlSession session = sqlSessionFactory.openSession();

    try {
      session.insert("RandomNumberRepo.masterInsert", randomNumberRepo);
    } finally {
      session.commit();
      session.close();
    }
  }

  public void masterDeleteAll() {
    SqlSession session = sqlSessionFactory.openSession();

    try {
      session.delete("RandomNumberRepo.masterDeleteAll");
    } finally {
      session.commit();
      session.close();
    }
  }

  public List<RandomNumberRepo> masterSelectAll() {
    List<RandomNumberRepo> list;

    try (SqlSession session = sqlSessionFactory.openSession()) {
      list = session.selectList("RandomNumberRepo.masterSelectAll");
    }

    return list;
  }

  public List<RandomNumberRepo> masterSelectNumByTimestamp(long timestamp) {
    List<RandomNumberRepo> list;
    try (SqlSession session = sqlSessionFactory.openSession()) {
      list = session.selectList("RandomNumberRepo.masterSelectNumByTimestamp", timestamp);
    }

    return list;
  }

  /**
   * for slave
   **/
  public void slaveInserts(List<RandomNumberRepo> randomNumberRepos) {

    SqlSession session = sqlSessionFactory.openSession();

    try {
      randomNumberRepos.forEach(
          randomNumberRepo -> session.insert("RandomNumberRepo.slaveInsert", randomNumberRepo)
      );
    } finally {
      session.commit();
      session.close();
    }
  }

  public long slaveSelectLastTimestamp() {
    long timestamp;
    try (SqlSession session = sqlSessionFactory.openSession()) {
      timestamp = session.selectOne("RandomNumberRepo.slaveSelectLastTimestamp");
    } catch (Throwable IgThrowable) {
      timestamp = 0;
    }

    return timestamp;
  }
}
