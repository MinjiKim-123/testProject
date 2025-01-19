# sync project
Redis와 JPA Lock을 사용하여 동시성 테스트를 진행하는 프로젝트입니다.


# 목차
- [JPA Lock] (#jpa-lock)
- [Redis Lock] (#redis-lock)


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
