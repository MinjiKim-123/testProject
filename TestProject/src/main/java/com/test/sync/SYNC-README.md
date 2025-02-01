# sync project
Redis와 JPA Lock을 사용하여 동시성 테스트를 진행하는 프로젝트입니다.

# JPA Lock
- ### 낙관적 락(Optimistic Lock) <br/>
데이터 갱신시 충돌이 발생하지 않을 것이라고 낙관적으로 보며 잠금을 거는 기법.<br/>
버저닝을 통해 관리하는 lock 방법이며,<br/>
조회시 버전과 수정 후 commit 시의 버전을 확인하여 버전이 다를 경우 수정사항을 반영하지 않고 오류를 발생시킨다.<br/>
충돌을 방지하기 위해 사용하는 락이므로 여러 사람이 동시에 접근하는 데이터에는 적합하지 않음.<br/>
DB Lock이 아닌 Application Level의 Lock이다.

- ### 비관적 락(Pessimistic Lock) <br/>
트랜잭션의 충돌이 자주 발생한다고 비관적으로 가정하고 잠금을 걸어 충돌을 예방하는 기법.<br/>
데이터 접근시 Shared lock 또는 Exclusive Lock을 이용하여 락을 걸며, <br/>
Repeatable Read 또는 Serializable 수준의 격리 레벨을 제공한다. <br/>
DB Level에서 레코드 자체에 Lock을 걸기 때문에 성능 저하 및 DeadLock이 발생할 수 있다.

##### - Shared lock(공유락)이란?
읽기 잠금.<br/>
한 트랜잭션에서 데이터를 읽고 있을 경우 다른 트랜잭션들은 해당 데이터를 조회만 가능하고 수정은 불가능하다.
##### - Exclusive lock(공유락)이란?
쓰기 잠금.<br/>
한 트랜잭션에서 데이터를 수정하고자 할 때 다른 트랜잭션은 해당 데이터를 조회 및 수정 모두 불가능하다.


# Redis Lock
- ### Lettuce Lock<br/>
Lettuce는 Spring redis의 기본 라이브러리.<br/>
스레드가 lock를 획득하기 위해 반복적으로 확인하며 시도하는<br/>
<strong>_spin lock_</strong>방식이기 때문에 락 획득을 위한 재시도 로직 구현이 필요하다.
```
try {
    int maxTryTime = 10; //최대 시도 횟수를 10번으로 지정
    boolean isGetLock = false;
    while (!isGetLock && maxTryTime > 0) { // lock을 얻을때까지 실행
      isGetLock = redisTemplate.opsForValue().setIfAbsent(lockKey, "lock", lettuceLock.timeout(),
          lettuceLock.timeUnit()); // 값이 없으면 set하는 setnx 명령어 사용
      maxTryTime--;
    }
    if(!isGetLock) { //최대 시도 횟수 내에 lock을 get하지 못 했을 경우 실패로 종료
      log.error("get lettuce lock failed.");
      return false;
    }
    return joinPoint.proceed();
  } finally {
    redisTemplate.delete(lockKey); // unlock처리
    log.info("Lettuce lock unlocked.");
}
```

- ### Redisson Lock<br/>
Redisson Lock은 pub/sub 방식으로 동작.<br/>
Redisson은 RLock이라는 인터페이스를 제공하기 때문에 쉽게 구현이 가능하고,<br/>
pub/sub 방식을 통해 지정한 waitTime까지 기다리며 락 획득 가능 시점에 락을 획득하기 때문에<br/>
지속적으로 락 획득 요청을 보내는 Lettuce의 spin lock방식보다는 redis에 부하가 덜 하다는 장점이 있다. 
```
 RLock lock = redissonClient.getLock(lockKey);
    
try {
    boolean isSucceed = lock.tryLock(redissonLock.waitTime(), redissonLock.leaseTime(), redissonLock.timeUnit()); //lock 획득 시도
    log.info("Redisson lock get tried. Result : " + isSucceed);
    if(!isSucceed) { //waitTime내에 lock을 획득 실패했을 경우
      log.info("trylock failed");
      return false;
    }
    
    return joinPoint.proceed();
} catch (InterruptedException e) {
    log.error(e.getMessage(), e);
    throw e;
}finally {
    try {
      lock.unlock(); //unlock처리
      log.info("Redisson unlocked.");
    }catch (Exception e) {
      log.error("Redisson unlock failed.", e);
    }
}
```
