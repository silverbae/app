package org.example.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.log4j.Logger;
import org.example.MyBatisConnectionFactory;
import org.example.mapper.RandomNumberDAO;
import org.example.mapper.RandomNumberRepo;

/**
 * 랜덤넘버 동기화(for master) - 1초 주기로 slave에 없는 랜덤넘버들을 전송 하여 동기화 시킨다.
 */
public class RandomNumberSync implements Runnable  {

  final static Logger log = Logger.getLogger("org.example");

  public RandomNumberSync()  {
  }

  private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

  @Override
  public void run() {
    log.info("start RandomNumberSync");

    RandomNumberDAO dao = new RandomNumberDAO(MyBatisConnectionFactory.getSqlSessionFactory());

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
      log.debug("error : " + e.getMessage());
    }
  }
}
