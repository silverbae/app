package org.example.dao;

import java.io.IOException;
import java.io.Reader;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MyBatisConnectionFactory {

  private static SqlSessionFactory sqlSessionFactory;

  static {
    try {
      String resource = "config/config.xml";
      Reader reader = Resources.getResourceAsReader(resource);

      if (sqlSessionFactory == null) {
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      }
    } catch (IOException fileNotFoundException) {
      fileNotFoundException.printStackTrace();
    }
  }

  public static SqlSessionFactory getSqlSessionFactory() {
    return sqlSessionFactory;
  }
}
