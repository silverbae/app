package org.example.application;

import org.apache.log4j.Logger;
import org.example.MyBatisConnectionFactory;
import org.example.model.RandomNumberRepo;
import org.example.persistence.RandomNumberDAO;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 랜덤넘버 동기화(for slave) - master로 부터 전송받은 데이터를 동기화 시킨다.
 */
public class RandomNumberClone {

  final static Logger log = Logger.getLogger("common");

  private RandomNumberClone() {
  }

  public static RandomNumberClone getInstance() {
    return LazyHolder.instance;
  }

  private static class LazyHolder {

    private static final RandomNumberClone instance = new RandomNumberClone();
  }

  private static final ExecutorService service = Executors.newSingleThreadExecutor();

  @SuppressWarnings("InfiniteLoopStatement")
  public void _start() {
    RandomNumberDAO dao = new RandomNumberDAO(MyBatisConnectionFactory.getSqlSessionFactory());

    try (ServerSocket ss = new ServerSocket()) {

      ss.bind(new InetSocketAddress(9993));

      while (true) {
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

                while (recv.readNBytes(bufferTimestamp, 0, 8) > 0
                    && recv.readNBytes(bufferNumber, 0, 4) > 0) {

                  long ts = ByteBuffer.wrap(bufferTimestamp).getLong();
                  int num = ByteBuffer.wrap(bufferNumber).getInt();

                  dao.slaveInsert(new RandomNumberRepo(ts, num));
                }
              } catch (Throwable e) {
                e.printStackTrace();
              }
            } catch (Throwable e) {
              e.printStackTrace();
            }

            log.debug("disconnected(" + s.getInetAddress() + ")");
          });

        } catch (Throwable e) {
          // 에러 발생시 콘솔 출력
          e.printStackTrace();
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  public void _stop() {
    service.shutdown();
  }

  public static void start() {
    getInstance()._start();
  }

  public static void stop() {
    getInstance()._stop();
  }
}
