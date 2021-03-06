# fishdelivery

Lv.2 개인평과 과제 -  회 배달 서비스

![image](https://user-images.githubusercontent.com/78421066/126853944-d8ae605d-e7c4-419f-ac66-16df8b3606ad.png)

# 온라인 회 배달 (회 배달 서비스)

# Table of contents

- [개인과제 - 회 배달 서비스](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석-설계)
  - [구현:](#구현)
    - [DDD 의 적용](#DDD의-적용)
    - [SAGA Pattern](#SAGA-Pattern)
    - [동기식 호출과 Fallback 처리](#동기식-호출과-Fallback-처리)
    - [비동기식 호출과 Eventual Consistency](#비동기식-호출과-Eventual-Consistency)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [API 게이트웨이](#API-게이트웨이)
  - [운영](#운영)
    - [CI/CD](#CI/CD)
    - [ConfigMap](#ConfigMap)
    - [Self-healing (Liveness Probe)](#self-healing-(liveness-probe))
    - [Zero-downtime deploy (Readiness Probe)](#Zerodowntime-deploy-(Readiness-Probe))
    - [동기식 호출 / Circuit Breaker / 장애격리](#동기식-호출-circuit-breaker-장애격리)
    - [Autoscale (HPA)](#Autoscale-(HPA))    

# 서비스 시나리오

기능적 요구사항
1. 고객이 상품(회)를 선택하여 주문(Order)한다
2. 고객이 결제(Pay)한다
3. 결제가 완료되면 주문 내역이 상점에 전달된다(fishstore)
4. 상점주인이 주문을 접수하고 상품(회)를 만든다.
5. 상품(회) 포장이 완료되면 상점소속배달기사가 배송(Delivery)을 시작한다.
6. 고객이 주문을 취소할 수 있다
7. 주문이 취소되면 배송 및 결제가 취소된다
8. 고객이 주문상태를 중간 중간 조회한다
9. 주문/배송상태가 바뀔 때마다 고객이 마이페이지에서 상태를 확인할 수 있다

비기능적 요구사항
1. 트랜잭션
  - 결제가 완료되어야만 주문이 완료된다 (결제가 되지 않은 주문건은 아예 거래가 성립되지 않아야 한다 Sync 호출)
2. 장애격리
  - 상점(fishstore) 기능이 수행되지 않더라도 주문(Order)은 365일 24시간 받을 수 있어야 한다 Async (event-driven), Eventual Consistency 
  - 결제시스템이 과중되면 사용자를 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다 Circuit breaker, fallback
3. 성능
  - 고객이 마이페이지에서 배송상태를 확인할 수 있어야 한다 CQRS

# 체크포인트

- 분석 설계

  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?

# 분석 설계  
## 전체 프로그램 구성
    전체 프로그램은 주문, 결제, 상점(회), 배송 서비스로 MSA 설계를 진행하였다. 
![image](https://user-images.githubusercontent.com/78421066/126854280-b572330a-e763-447a-a0f0-2b863726b621.png)

## 이벤트스토밍: 
 - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가? 각 도메인 이벤트가 의미있는 수준으로 정의되었는가? 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
 
      - Event는 발생한 사실, 결과, Output을 의미 하며, 스티커는 **오렌지색**을 사용하였다. 또한 이해 가능한 의미로 표현을 하였고 동사의 과거형(p.p)로 표현 하였고 타임라인 끝에 위치 시켰다. 도메인별 발생하는 행위별(주문, 취소, 접수, 배송시작 등)로 이벤트를 도출하였다. 마지막으로 중복되는 이벤트(주문방식별)는 제거하였다.
      - Command의 의자결정, Input, API, UI버튼을 의미하며 스티커는 **하늘색**을 사용하였다. 
      - Policy의 경우 Event에 대한 반응(Reaction)을 뜻하며 스티커는 **라일락색**을 사용하였다. kafka를 사용하여 Event에 대한 메세지를 수신하였다.
      - Aggregate의 경우 비즈니스 로직 처리를 하는 시스템, 데이터 객체를 의미한다. 스티커는 **노랑색**을 사용하였다. JPA를 이용하였고 H2, MariaDB를 사용하여 데이터를 저장 하였다.
      - View는 행위와 결정을 하기 위해 유저가 참고하는 데이터로 CQRS에 사용된다. 스티커는 **녹색**을 사용하였다.    
 
 - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?
 
   - 기능적 요구사항 : 고객이 상품(회)를 받기 위한 일련의 과정을 도메인으로 나눴다. 도메인은 총 4개 주문, 결제, 상점, 배송으로 나눠지며 고객이 주문상태를 조회하기 위한 CQRS 기능을 주문 Bounded Context에 구현 하였다.
    - 비기능적 요구사항 : 결제가 완료되야 주문이 완료되기 때문에 req-res방식으로 동기식 호출을 진행하였다. 또한 상점의 서비스가 잠시 장애가 있더라도 고객은 주문을 할 수 있어야 하기에 Event-driven한 비동기식 방식(kafka)을 채택하였다. 결제 시스템의 성능이 떨어질때 잠시 오더 서비스가 늦게 주문 요청을 할 수 있는 Circuit breaker기능을 fallback 함수를 통해 반영하였다. 고객은 중간중간 자신의 배송상태를 알아야 하기에 CQRS를 통해 확인 할수 있다. 배송상태는 아래와 같다.
![image](https://user-images.githubusercontent.com/78421066/126854238-1c577c6b-d998-486b-bff1-61f7fc45972d.png)

     
## 서브 도메인, 바운디드 컨텍스트 분리
 - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
   
   domain의 경우 24시간 가동이 필요한 주문(core)과 결제, 상점, 배송(supporting)으로 분리 하였다. 또한 DDD관점으로 결제, 상점, 배송의 Bounded Context를 분리하였다.
   ![image](https://user-images.githubusercontent.com/78421066/126854267-9f963313-ed9f-4367-8335-69ff8c4969cc.png)
  
 - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    payment의 경우 안정성이 중요하기에 database를 마리아DB로 적용하기로 결정 하였다.

## 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
 - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
   위에 사진에서 볼 수 있듯이 가장 중요한 주문 서비스를 Core 도메인으로 두고 나머지 부분을 Supporting 도메인으로 설정 하였다.
 - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
   결제가 되야 주문이 완료되기에 주문을 하고 결제가 완료되는 일련의 이벤트를 req-res 방식으로 설계 하였다.
 - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
   pub-sub 방식으로 구현하였기에 서포팅 이벤트가 제거되더라도 기존 서비스 작동에 이상은 없다.
 - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
   서비스별 데이터베이스를 따로 사용하기에 신규 서비스 추가시 영향이 없다.
 - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?
   각 서비스마다 **orderId**라는 컬럼이 있다. **orderId** 컬럼을 Correlation-key로 설정하였다.

## 헥사고날 아키텍처
 - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
![image](https://user-images.githubusercontent.com/78421066/127098106-d2832388-da3b-4518-8ecf-0cae2d5519c5.png)
   - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
   - 호출관계에서 Pub/Sub 과 Req/Resp 를 구분함
   - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐

# 구현
## DDD의 적용
  - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가?
   
각 도메인별(order, payment, fishstore, delivery로 Entity를 구성 하였다. key값의 경우 GenerationType.AUTO 전략을 사용하여 주문시 자동적으로 키 값이 증가하도록 하였다. 
``` 
@Entity
@Table(name="Order_table")
public class Order {    

@Id
@GeneratedValue(strategy=GenerationType.AUTO)
private Long orderId;
private String customerName;
private String fishName;
private Integer qty;
private Integer telephone;
private String address;
private String status;
@PostUpdate
public void onPostUpdate(){
  OrderCanceled orderCanceled = new OrderCanceled();
  BeanUtils.copyProperties(this, orderCanceled);
  orderCanceled.publishAfterCommit();
}
```
    
각 도메인별로 JPA를 이용하기 위해 Repository 인터페이스를 이용하였다. Correlation-key는 orderId이기 때문에 orderId로 조회가 가능한 쿼리 메소드를 구현 하였다.
    
```
@RepositoryRestResource(collectionResourceRel="payments", path="payments")
public interface PaymentRepository extends PagingAndSortingRepository<Payment, Long>{
/* 한용선 쿼리 메소드를 이용하여 검색 find + 엔티티이름(생략가능) + By + 변수 이름 */
Payment findByOrderId(long orderId);
}
```
    
  - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    
```
미구현
```
    
  - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
    
Ubiquitous Language(보편 언어)는 도메인 전문가, 아키텍트, 개발자 등 프로젝트 구성원 모두에게 공유된 언어로써 의사소통이 쉬운 언어를 선택 하였다.    
```
주문 : OrderPlaced
지불 : PayApproved
주문접수 : OrderTaken
주문취소 : OrderCanceled
```
  
## SAGA Pattern

SAGA Pattern이란 분산 트랜잭션 시나리오의 마이크로 서비스에서 데이터 일관성을 관리 하는 방법입니다. Saga는 각 서비스를 업데이트 하고 메시지 또는 이벤트를 게시 하여 다음 트랜잭션 단계를 트리거하는 일련의 트랜잭션 입니다.

![image](https://user-images.githubusercontent.com/78421066/127072004-9c72db60-1b40-4ab6-9d30-27a5d3193a36.png)

빨강색으로 표기된 주문이 발생하면 주문(order)서비스에서 결제(payment)서비스로 이벤트를 보낸다.

![image](https://user-images.githubusercontent.com/78421066/127098482-50502219-61cb-4566-971e-d68243c44b57.png)

![image](https://user-images.githubusercontent.com/78421066/127072584-05916674-7fae-4efb-8272-2fcf6adaac67.png)

상점(fishstore)에서 주문이 가능함이 확인되면 접수를 진행하고 포장과 함께 배송(delivery)서비스로 가게 된다.

![image](https://user-images.githubusercontent.com/78421066/127072790-8a8d2679-50b6-4f3d-8a4b-54dcb575ac63.png)

![image](https://user-images.githubusercontent.com/78421066/127072836-c85c2033-db01-433b-ac5b-b5e5846d3a86.png)

주문 취소의 경우 취소 이벤트가 발생하게 되면 모든 서비스에게 취소 이벤트가 전달되게 된다.

![image](https://user-images.githubusercontent.com/78421066/127073207-821f38f4-62fb-4a41-8a2a-9d8469fab5b1.png)

![image](https://user-images.githubusercontent.com/78421066/127073243-bf4d6c49-3216-4d45-97c7-bff9115d5d2e.png)
  
## 동기식 호출과 Fallback 처리
  - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)

주문과 결제는 하나의 STEP으로 이루어져야 하기 때문에 Req-Res 방식으로 진행하였다. 해당 방식을 이용하기 위해서 order -> payment 서비스를 호출할때 @FeingClient를 이용하였다.
```
@FeignClient(name="payment", url="http://localhost:8082", fallback = PaymentServiceFallback.class)
public interface PaymentService {
    @RequestMapping(method= RequestMethod.POST, path="/payments")
    public void pay(@RequestBody Payment payment);
}
```

- 서킷브레이커를 통하여 장애를 격리시킬 수 있는가?
서킷브레이커를 만들기 위하여 @FeignClient에서 hystrix fallback PaymentServiceFallback.class를 지정 하였다. fallback이 되지 위해서 임의로 payment class에 sleep을 줬고 
hystrix시간도 함께 설정 하였다.
```
#PaymentService fallback class 지정
@FeignClient(name="payment", url="http://localhost:8082", fallback = PaymentServiceFallback.class)
public interface PaymentService {
    @RequestMapping(method= RequestMethod.POST, path="/payments")
    public void pay(@RequestBody Payment payment);
}

# application.yml hystrix 명령의 기본 timeout을 0.6초 지정
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds: 600

# payment 클래스 Entity에 insert 일어나기 전에 Circuit breaker 실행을 위해 sleep을 줬다. */
@PrePersist
public void onPrePersist(){
     try{
          Thread.currentThread().sleep((long) (700 + Math.random() * 220));
     } catch (InterruptedException e) {
          e.printStackTrace();
     }
}
```
  
## 비동기식 호출과 Eventual Consistency
  - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?

주문 및 포장이 시작 Command가 일어나면 이벤트가 발생하고 각 서비스가 원하는 eventType을 가져가 연동이 된다.
```
#주문
http POST localhost:8081/orders customerName="HanYongsun" fishName="flatfish" qty=1 telephone="01012341234" address="kyungkido sungnamsi" status="paid"
#포장
http PATCH localhost:8083/fishstores/1 status="prepared"

#발생한 이벤트
{"eventType":"OrderPlaced","timestamp":"20210724163545","orderId":1,"customerName":"HanYongsun","fishName":"flatfish","qty":1,"telephone":1012341234,"address":"kyungkido sungnamsi","status":"paid"}
{"eventType":"PayApproved","timestamp":"20210724163547","paymentId":1,"orderId":1,"customerName":"HanYongsun","fishName":"flatfish","qty":1,"telephone":1012341234,"address":"kyungkido sungnamsi","status":"paid"}
{"eventType":"OrderTaken","timestamp":"20210724163843","fishOrderId":1,"orderId":1,"customerName":"HanYongsun","fishName":"flatfish","qty":1,"telephone":1012341234,"address":"kyungkido sungnamsi","status":"prepared"}
{"eventType":"DeliveryStarted","timestamp":"20210724163843","deliveryId":1,"orderId":1,"status":"delivered"}
```

  - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?

모든 주문은 orderId를 기준으로 구별 할 수 있게 만들었다.(Correlation-key) 서비스별 Repository에 findByOrderId 함수를 구현 하였다. 주문 취소시 해당 함수를 통해 고객이 원하는 취소건을 구별 할 수 있다.
```
@StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_CancelOrder(@Payload OrderCanceled orderCanceled){

        if(!orderCanceled.validate()) return;

        System.out.println("\n\n##### listener CancelOrder : " + orderCanceled.toJson() + "\n\n");

        /* 한용선 취소상태일때 상태 업데이트하여 저장 */
        /* save 함수의 경우 저장과 수정이 가능하다. */
        Payment payment = paymentRepository.findByOrderId(orderCanceled.getOrderId());
        payment.setStatus(orderCanceled.getStatus());
        paymentRepository.save(payment);
    }
```

  - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?

delivery 서비스를 실행하지 않고 주문 및 상품준비를 마무리 하였다. 아래 캡쳐본과 같이 OrderTakan 이벤트가 생성되고 DeliveryStarted는 생성되지 않음을 알 수 있다.

![image](https://user-images.githubusercontent.com/78421066/126862270-a00448e2-6b67-4b4b-abfe-c078a0df6b03.png)

delivery 서비스를 실행시키면 자동으로 이벤트가 수신 됨을 알 수 있다.(시간 다름 확인)

![image](https://user-images.githubusercontent.com/78421066/126862333-a2a6b9bc-fb10-4205-a2e8-f6056df0d3ea.png)

  - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가?

배송(delievery)서비스의 포트 추가(기존:8084, 추가:8094)하여 2개의 노드로 배송서비스를 실행한다. fishdelivery topic의 partition은 1개이기 때문에 기존 8084 포트의 서비스만 partition이 할당된다.

![image](https://user-images.githubusercontent.com/78421066/126863545-c1713cd6-b3d2-49ca-b74a-672808daf2a1.png)
 
 배송 이벤트 발생시 8084 포트에만 주문이 들어오게 되어 중복이 발생 안함을 확인 할 수 있다.

![image](https://user-images.githubusercontent.com/78421066/126863755-4799cb2f-b919-4df4-976e-109034025f50.png)
 
  - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?
CQRS(Command and Query Responsibility Segregation)는 시스템의 상태를 변경하는 작업과 시스템의 상태를 반환하는 작업의 책임을 분리하는 것이다. 설계시 order서비스내에 구성하였고 Entity는 'Mypage_table'로 만들었다.

```
#Entity 생성
@Getter
@Setter
@Entity
@Table(name="MyPage_table")
public class MyPage {

        @Id
        private Long orderId;
        private String customerName;
        private String fishName;
        private Integer qty;
        private Integer telephone;
        private String address;
        private String status;
}

#Repository 관련 코드
public interface MyPageRepository extends CrudRepository<MyPage, Long> {

    List<MyPage> findByStatus(String status);

}

#Policy핸들러 관련 코드
@Autowired
    private MyPageRepository myPageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderPlaced_then_CREATE_1 (@Payload OrderPlaced orderPlaced) {
        try {

            if (!orderPlaced.validate()) return;

            // view 객체 생성
            MyPage myPage = new MyPage();
            // view 객체에 이벤트의 Value 를 set 함
            myPage.setOrderId(orderPlaced.getOrderId());
            myPage.setCustomerName(orderPlaced.getCustomerName());
            myPage.setFishName(orderPlaced.getFishName());
            myPage.setQty(orderPlaced.getQty());
            myPage.setTelephone(orderPlaced.getTelephone());
            myPage.setAddress(orderPlaced.getAddress());
            myPage.setStatus(orderPlaced.getStatus());
            // view 레파지 토리에 save
            myPageRepository.save(myPage);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
```

이벤트를 발생하고 MyPages를 조회하여 주문 대상들을 확인 할 수 있다.

![image](https://user-images.githubusercontent.com/78421066/126864589-b74ac9a9-f4b8-451e-9dc4-6655e6aeaff3.png)

## 폴리글랏 퍼시스턴스
Payment서비스의 경우 결제 데이터가 저장되어 있기 때문에 H2보단 안정성이 더 좋은 MariaDB를 사용하기로 결정 하였다. 해당 DB는 AWS에서 제공하는 RDS서비스에서 구축하였다.
![image](https://user-images.githubusercontent.com/78421066/127096086-48f6cdc7-12c9-40bb-8ed6-1984e55b9acf.png)

소스의 경우도 mariadb 의존성을 주입 하고 rds와 접근하기 위한 url도 넣어 주었다.
![image](https://user-images.githubusercontent.com/78421066/127096186-0f7aa6e9-81ea-4b71-8424-edff5cf6b03a.png)

![image](https://user-images.githubusercontent.com/78421066/127096232-f3ae92da-e732-4ff5-984a-c6322fd2b7f1.png)

사유는 알 수 없으나 mariadb의 경우 pk가 자동으로 채번되지 않았다. 그래서 sequence 테이블을 생성하니 데이터가 정상적으로 들어갔다.
![image](https://user-images.githubusercontent.com/78421066/127096432-e19b4799-628f-4d08-bd8b-b342c6016b2a.png)

주문을 넣으면 PAYMENT 테이블에 데이터가 들어감을 알 수가 있다.
![image](https://user-images.githubusercontent.com/78421066/127096513-d444c0f5-4b1b-402e-a399-57087e2bdcbe.png)

  
## API 게이트웨이
  - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
아래는 MSAEZ를 통해 자동 생성된 gateway 서비스의 application.yml이며, 마이크로서비스들의 진입점을 통일하여 URL Path에 따라서 마이크로서비스별 서로 다른 포트로 라우팅시키도록 설정되었다.
gateway 서비스의 application.yml 파일
```
server:
  port: 8088

---
spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: order
          uri: http://localhost:8081
          predicates:
            - Path=/orders/**, /myPages/**
        - id: payment
          uri: http://localhost:8082
          predicates:
            - Path=/payments/** 
        - id: fishstore
          uri: http://localhost:8083
          predicates:
            - Path=/fishstores/** 
        - id: delivery
          uri: http://localhost:8084
          predicates:
            - Path=/deliveries/** 
```

Gateway 포트인 8088을 통해서 주문을 생성시켜 8081 포트에서 서비스되고 있는 주문서비스(order)가 정상 동작함을 확인함

![image](https://user-images.githubusercontent.com/78421066/126899718-1c386cf3-748c-41b8-970a-4ca2ee46ccb1.png)
 
# 운영
## CI/CD

지속적 통합(CI)의 경우 Git을 이용하였다. 완성된 소스는 GitHub에 올려 놓았고 업데이트시 Comment도 기입 하였다.

![image](https://user-images.githubusercontent.com/78421066/126940482-bc28ea13-18d8-4b75-82e9-53feacee964f.png)

지속적 배포(CD)의 경우 AWS CodeBuild를 이용하였다. 각 마이크로서비스별로 buildspec을 이용하여 도커이미지 파일 및 컨테이너를 생성하였다. 이미지파일은 AWS ECR 서비스를 이용하여 저장하였다.

![image](https://user-images.githubusercontent.com/78421066/126941039-358fd4c6-24e5-4b85-a8cc-f3d14ca19c8d.png)
![image](https://user-images.githubusercontent.com/78421066/126941113-9d4683b6-ad59-4ac0-9b75-c831de579b9c.png)
![image](https://user-images.githubusercontent.com/78421066/126941252-4f40e09f-47ac-476a-8c44-80f2a8e7866a.png)

마지막으로 쿠버네티스를 이용하여 컨테이너들을 관리하였고 AWS EKS 서비스를 이용하였다.

![image](https://user-images.githubusercontent.com/78421066/126941292-0e68bde4-4a64-4dc1-bbc3-0b2a54885407.png)

부여 받은 계정으로 AWS CLI에서 인증 후 EKS 클러스터에서 fishdelivery 마이크로서비스별 컨테이너를 조회한 모습이다.
![image](https://user-images.githubusercontent.com/78421066/126941549-c995e94c-4889-4581-b294-1470e6e89bd7.png)

## ConfigMap

Order서비스에서 Req-Res적용을 위해 payment URL이 필요한데 해당 URL(http://payment:8080)을 환경변수에 넣어 보았다.
ConfigMap 코드 이다.

```
apiVersion: v1
kind: ConfigMap
metadata:
  name: payment-url
data:
  url: http://payment:8080
```

해당 ConfigMap을 적용하기 위해 buildSpec에 환경변수를 추가 하였다. 코드에 보면 configMapKeyRef가 보이는데 configMap의 이름과 url변수에 저장된 키값을 참고 하겠다는 의미이다.

![image](https://user-images.githubusercontent.com/78421066/127099138-b011ad4f-ac70-433b-a7ff-e046973ffa7f.png)

![image](https://user-images.githubusercontent.com/78421066/127075033-021e2ed6-0819-415d-ade2-9c631155cd5f.png)

다시 배포를 한 이후 order서비스 pod에 들어가서 URL 변수의 키값을 확인 할 수가 있다.

![image](https://user-images.githubusercontent.com/78421066/127076421-c3c543f9-6907-48e7-82e0-c32e61affce9.png)

## self-healing (liveness probe)
  - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    
Liveness Probe를 진행하기 위해서 아래의 시나리오로 테스트를 진행하였다.
  
시나리오는 아래와 같다.
1.delivery Pod 실행시 /tmp/healthy 파일이 존재하는지 확인한다.(체크시간은 아래 주석표기)

```
apiVersion: v1
kind: Pod
metadata:
  name: delivery
  labels:
    app: delivery
spec:
  containers:
  - name: delivery
    image: 879772956301.dkr.ecr.ca-central-1.amazonaws.com/user25-ecr:delivery
    livenessProbe:
      exec:
        command:
        - cat 
        - /tmp/healthy
      initialDelaySeconds: 15  # 15초 후 시작
      periodSeconds: 5 # 5초단위로 검사
      failureThreshold: 4 # 4번 실패시 1번 Restart
```

2.체크 중간에 delivery Pod에 접속하여 파일을 넣는다.

![image](https://user-images.githubusercontent.com/78421066/126961760-94a765ca-2cfc-4606-8df2-350601657de3.png)

3.delivery Pod가 정상적으로 실행됨을 확인 한다.

Restart 2회 확인, kubectl describe 명령어로 확인시 정상 실행 확인

![image](https://user-images.githubusercontent.com/78421066/126961897-6485dcba-ce6c-4665-9e5f-05a19c3545c7.png)

![image](https://user-images.githubusercontent.com/78421066/126962170-916ad806-aed8-45d3-ab19-cac6ba25d876.png)
 
  
## Zerodowntime deploy (Readiness Probe)
  - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 

fishstore의 이미지 및 속성을 변경하면서 readinessProbe 속성도 넣었다. 또한 replicas를 2 -> 1로 줄이며 pod의 숫자도 줄여보았다.

```
kubectl apply -f readiness_probe.yml 
```
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: fishstore
  labels:
    app: fishstore
spec:
  replicas: 1
  selector:
    matchLabels:
      app: fishstore
  template:
    metadata:
      labels:
        app: fishstore
    spec:
      containers:
      - name: fishstore
        image: 879772956301.dkr.ecr.ca-central-1.amazonaws.com/user25-ecr:Readiness
        ports:
        - containerPort: 8080
        readinessProbe:
          httpGet:
            path: '/fishstores'
            port: 8080
          initialDelaySeconds: 10
          timeoutSeconds: 2
          periodSeconds: 5
          failureThreshold: 3  
```

이렇게 속성이 변경될때 watch명령어를 사용하여 pod의 변동을 살펴 보았다.
![image](https://user-images.githubusercontent.com/78421066/127002688-f6470762-cb3e-41c5-8d6c-40065e1b7d19.png)

마지막으로 siege부하를 줘서 무중단 배포가 됨을 확인 하였다.
```
siege -c100 -t30S -v --content-type "application/json" 'a710f5c7dd5824c66a6add5cdb3d7693-1620655872.ca-central-1.elb.amazonaws.com:8080/fishstores'
```
![image](https://user-images.githubusercontent.com/78421066/127003092-d791c1e8-335d-4ca7-8783-cd576cf14be8.png)

  
## 동기식 호출 circuit breaker 장애격리
  - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함.오더 요청이 과도할 경우 서킷 브레이크를 통해 장애 격리를 하려고 한다.
Hystrix 를 설정: 요청처리 쓰레드에서 처리시간이 610 ms가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정

![image](https://user-images.githubusercontent.com/78421066/127099570-11accc15-22ec-49c0-bdae-5c0090c5ff6d.png)

![image](https://user-images.githubusercontent.com/78421066/127099637-15c86c52-a564-430c-8fe6-2cbc7be8d2de.png)

 
부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인: 동시사용자 100명 30초 동안 실시
```
siege -c100 -t30S -v --content-type "application/json" 'a710f5c7dd5824c66a6add5cdb3d7693-1620655872.ca-central-1.elb.amazonaws.com:8080/orders POST {"customerName": "HanYongsun", "fishName": "flatfish", "qty": 1, "telephone": "01012341234", "address": "kyungkido sungnamsi", "status": "paid"}'
```

![image](https://user-images.githubusercontent.com/78421066/127003783-f2ffb4b5-42c4-404d-92d4-4601684fc58a.png)
  
## Autoscale (HPA)
  - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
order서비스의 cpu사용량이 20%가 되면 최대 3개까지 pod를 확장하도록 HPA설정을 하였다.

![image](https://user-images.githubusercontent.com/78421066/127004714-17193bae-e3f8-4d4b-bf0f-10038998c76c.png)

현재 pod의 갯수는 1개 이다.

![image](https://user-images.githubusercontent.com/78421066/127004332-ab516303-379c-4e00-b2dd-8b81440515d7.png)

siege를 이용하여 부하를 주었다.
```
siege -c200 -t40S -v --content-type "application/json" 'a710f5c7dd5824c66a6add5cdb3d7693-1620655872.ca-central-1.elb.amazonaws.com:8080/orders'
```

현재 pod의 갯수가 3개로 늘었다.

![image](https://user-images.githubusercontent.com/78421066/127008300-160129bb-3bdf-44a0-aed8-239f33aa6e05.png)
