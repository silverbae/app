package org.example.mapper;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.config.SchemaConfig;
import com.wix.mysql.distribution.Version;
import java.time.ZoneId;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class EmbeddedMySql {

  final static Logger log = Logger.getLogger("common");

  static EmbeddedMysql mySqlServer = null;

  @BeforeAll
  public static void mySqlStart() {

    log.info("start embedded mysql");

    MysqldConfig config = MysqldConfig.aMysqldConfig(Version.v8_0_17)
        .withPort(3306)
        .withUser("admin", "admin")
        .withTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()))
        .build();

    SchemaConfig.Builder schemaConfig = SchemaConfig.aSchemaConfig("test").withCommands("""
        CREATE TABLE if not exists RandomNumberRepo(timestamp BIGINT, num INT);
        CREATE TABLE if not exists BOOK(id DOUBLE, name VARCHAR(20));        
        """);
    mySqlServer = EmbeddedMysql.anEmbeddedMysql(config).addSchema(schemaConfig.build()).start();

  }

  @AfterAll
  public static void mySqlStop() {
    log.info("stop embedded mysql");

    if (mySqlServer != null) {
      mySqlServer.stop();
    }
  }
}
