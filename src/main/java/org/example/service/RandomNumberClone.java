package org.example.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.example.dao.MyBatisConnectionFactory;
import org.example.mapper.RandomNumberDAO;
import org.example.mapper.RandomNumberRepo;

/**
 * 랜덤넘버 동기화(for slave) - master로 부터 전송받은 데이터를 동기화 시킨다.
 */
public class RandomNumberClone implements Runnable  {

  final static Logger log = Logger.getLogger("org.example");
  private static final ExecutorService service = Executors.newSingleThreadExecutor();
  private boolean isRunning = true;

  public RandomNumberClone() {
  }

  @Override
  public void run() {
    RandomNumberDAO dao = new RandomNumberDAO(MyBatisConnectionFactory.getSqlSessionFactory());

    try (ServerSocket ss = new ServerSocket()) {

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          isRunning = false;
          ss.close();
        } catch(Throwable ignored) {
        }
      }));

      ss.bind(new InetSocketAddress(9993));

      while (isRunning) {
        try {
          Socket s = ss.accept();

          service.submit(() -> {
            log.debug("connected(" + s.getInetAddress() + ")");

            try (OutputStream send = s.getOutputStream(); InputStream recv = s.getInputStream()) {

              // 현재 timestamp 포지션 전송
              long currentTimeStamp = dao.slaveSelectLastTimestamp();
              byte[] bufferCurrentTimestamp = ByteBuffer.allocate(8).putLong(currentTimeStamp)
                  .array();
              send.write(bufferCurrentTimestamp);

              // rn들을 받아온다.
              try {
                byte[] bufferTimestamp = {0, 0, 0, 0, 0, 0, 0, 0};
                byte[] bufferNumber = {0, 0, 0, 0};

                List<RandomNumberRepo> randomNumberRepos = new ArrayList<>();

                while (recv.readNBytes(bufferTimestamp, 0, 8) > 0
                    && recv.readNBytes(bufferNumber, 0, 4) > 0) {

                  long ts = ByteBuffer.wrap(bufferTimestamp).getLong();
                  int num = ByteBuffer.wrap(bufferNumber).getInt();

                  randomNumberRepos.add(new RandomNumberRepo(ts, num));
                }

                dao.slaveInserts(randomNumberRepos);

              } catch (Throwable e) {
                log.debug("error : " + e.getMessage());
              }
            } catch (Throwable e) {
              log.debug("error : " + e.getMessage());
            }

            log.debug("disconnected(" + s.getInetAddress() + ")");
          });

        } catch (Throwable e) {
          log.debug("error : " + e.getMessage());
        }
      }
    } catch (Throwable e) {
      log.debug("error : " + e.getMessage());
    }

    log.info("end");
  }
}
