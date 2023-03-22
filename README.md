## 빌드
```
$ gradle clean
$ gradle jar
```

## 실행환경 구성
Gradle 7.5.1
JVM: 19.0.2 (Oracle Corporation 19.0.2+7-44)

### mysql 실행
```
mysql 실행[update.patch](..%2F..%2FDownloads%2Fupdate.patch)
$ docker run -itd --rm --name mysql -e TZ=Asia/Seoul -e MYSQL_ROOT_PASSWORD=1234 -e MYSQL_DATABASE=test -e MYSQL_PASSWORD=1234 -p 3306:3306 mysql:8.0.32

테스트용 테이블 삽입
$ docker exec -it mysql bash
$ mysql -u root --password=1234 test -e 'CREATE TABLE if not exists RandomNumberRepo(timestamp BIGINT, num INT);'
$ mysql -u root --password=1234 test -e 'CREATE TABLE if not exists RandomNumberRepoSlave(timestamp BIGINT, num INT);'
```

## 실행
#### 1. master
```
$ java -jar ./build/libs/app-1.0-SNAPSHOT.jar master
```

#### 2. slave
```
$ java -jar ./build/libs/app-1.0-SNAPSHOT.jar slave
```

## 모니터링
### 동기화 확인 sql
```
$ docker exec -it mysql bash
$ mysql -u root --password=1234 test 

쿼리를 실행하면 마스터에만 있는 데이터만 출력한다.
$ mysql>
SELECT  'master' AS `set`, DATE_FORMAT(FROM_UNIXTIME(r.timestamp/1000), '%Y-%m-%d %H:%i:%S.%f') AS `datetime`, r.*
FROM    RandomNumberRepo r
WHERE   ROW(r.timestamp, r.num) NOT IN
(
SELECT  timestamp, num
FROM    RandomNumberRepoSlave
)
UNION ALL
SELECT  'slave' AS `set`, DATE_FORMAT(FROM_UNIXTIME(t.timestamp/1000), '%Y-%m-%d %H:%i:%S.%f') AS `datetime`, t.*
FROM    RandomNumberRepoSlave t
WHERE   ROW(t.timestamp, t.num) NOT IN
(
SELECT  timestamp, num
FROM    RandomNumberRepo
);

```