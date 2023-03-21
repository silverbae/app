package org.example.application;

import org.apache.log4j.Logger;
import org.example.MyBatisConnectionFactory;
import org.example.model.RandomNumberRepo;
import org.example.persistence.RandomNumberDAO;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 랜덤넘버 동기화(for master) - 1초 주기로 slave에 없는 랜덤넘버들을 전송 하여 동기화 시킨다.
 */
public class RandomNumberSync {

  final static Logger log = Logger.getLogger("common");

  private RandomNumberSync() {
  }

  public static RandomNumberSync getInstance() {
    return LazyHolder.instance;
  }

  private static class LazyHolder {

    private static final RandomNumberSync instance = new RandomNumberSync();
  }

  private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

  private void _start() {
    log.info("start RandomNumberSync");

    RandomNumberDAO dao = new RandomNumberDAO(MyBatisConnectionFactory.getSqlSessionFactory());

    service.scheduleAtFixedRate(() -> {
      log.debug("start sync");

      // slave에 연결하여 현재 저장된 데이터의 최종 timestamp를 받는다.
      try (Socket c = new Socket()) {
        c.connect(new InetSocketAddress("localhost", 9993));

        try (OutputStream send = c.getOutputStream(); InputStream recv = c.getInputStream()) {
          byte[] buffer = {0, 0, 0, 0, 0, 0, 0, 0};

          // read timestamp position
          if (recv.read(buffer, 0, buffer.length) > 0) {
            long timestamp = ByteBuffer.wrap(buffer).getLong();

            // db에서 지정한 timestamp보다 큰것을 모두 가져온다.
            List<RandomNumberRepo> rnList = dao.masterSelectNumByTimestamp(timestamp);

            // 가져온 데이터를 모두 전송한다.
            rnList.forEach(rn -> {
              try {
                send.write(ByteBuffer.allocate(8).putLong(rn.timestamp).array());
                send.write(ByteBuffer.allocate(4).putInt(rn.num).array());
              } catch (Throwable ignored) {
              }
            });
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }

      log.debug("end sync");
    }, 0, 1000, TimeUnit.MILLISECONDS);
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