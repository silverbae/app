package org.example.model;

import java.util.concurrent.ThreadLocalRandom;

public class RandomNumberRepo {

  public long timestamp;
  public int num;

  public RandomNumberRepo() {
    this.timestamp = System.currentTimeMillis();
    this.num = Math.abs(ThreadLocalRandom.current().nextInt());
  }

  public RandomNumberRepo(long timestamp, int num) {
    this.timestamp = timestamp;
    this.num = num;
  }
}
