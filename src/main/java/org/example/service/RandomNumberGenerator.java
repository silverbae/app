package org.example.service;

import org.example.MyBatisConnectionFactory;
import org.example.mapper.RandomNumberDAO;
import org.example.mapper.RandomNumberRepo;

/**
 * 랜덤넘버 생성기 - 0.1초 주기로 양의 정수를 생성하여 db에 저장 한다.
 */
public class RandomNumberGenerator implements Runnable {

  private final RandomNumberDAO dao;

  public RandomNumberGenerator() {
    dao = new RandomNumberDAO(MyBatisConnectionFactory.getSqlSessionFactory());
  }
  @Override
  public void run() {
    dao.masterInsert(new RandomNumberRepo());
  }
}
