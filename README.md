# 과일 주문 서비스
![image](https://user-images.githubusercontent.com/27180840/131888372-c52390ac-5f22-4da3-b8c6-44c80ddb16c7.png)

## 서비스 시나리오

### 기능적 요구 사항
1. 고객이 과일을 주문한다.
2. 고객이 결제한다.
3. 결제가 되면 배송이 시작된다.
4. 고객이 주문현황을 조회한다.
5. 결제가 성공시 배송이 가능하다.
6. 고객이 주문을 취소할 수 있다.
7. 고객이 주문을 취소하면 결제도 취소된다.

### 비기능적 요구 사항
1. 트랜잭션 
   - 고객은 결제가 완료되어야 배송 서비스 이용이 가능하다. (Sync)

2. 장애격리 
   - 과일 주문 서비스는 24시간 이용이 가능하다 (Async 호출-event-driven)
   - 결제 서비스가 과중되면 잠시 후에 하도록 유도한다. (Circuit breaker, fallback)

3. 성능
   - 고객이 주문한 내역의 진행현황을 확인할 수 있다. (CQRS)
   - 주문/걸제상태가 바뀔 때 마다 MyPage에서 상태를 확인할 수 있다.(Event Driven)
   
*****

## 분석/설계

### AS-IS 조직 (Horizontally-Aligned)

![image](https://user-images.githubusercontent.com/27180840/131845338-b5e0c548-1070-419e-b902-9f8ca8de1fd8.png)

### TO-BE 조직 (Vertically-Aligned)

![image](https://user-images.githubusercontent.com/27180840/131845668-3921794c-0b8d-4bae-84b9-ab0bd7b2fb5a.png)


### Event Storming 결과
MSAEz 로 모델링한 이벤트스토밍 결과:
http://www.msaez.io/#/storming/3CCWjZexX3Y7Ypm85RPzPTQIPLg1/113c0155f10101dd4e75418ff9cf25fe

### 이벤트스토밍 - Event

![image](https://user-images.githubusercontent.com/27180840/131846689-ad71f683-d921-42e5-9e38-67f7e15f09df.png)

### 이벤트스토밍 – 비적격 이벤트 제거

![image](https://user-images.githubusercontent.com/27180840/131846981-c706b08f-c2ac-4c3d-80ce-2180ccb60fbe.png)

### 이벤트스토밍 - Actor, Command

![image](https://user-images.githubusercontent.com/27180840/131900057-fefc78ac-f83f-4e62-82e1-d4896fee3144.png)

### 이벤트스토밍 - Aggregate

![image](https://user-images.githubusercontent.com/27180840/131900208-d83160fc-ee80-4569-b9a9-1b678f703355.png)

### 이벤트스토밍 - Bounded Context

![image](https://user-images.githubusercontent.com/27180840/131900317-867fcd9c-3414-418a-9454-dcc0c94a7361.png)

### 이벤트스토밍 - Policy

![image](https://user-images.githubusercontent.com/27180840/131900425-1479b2d8-d306-4eb2-adf9-4b550b4c33aa.png)

### 이벤트스토밍 - Context Mapping

![image](https://user-images.githubusercontent.com/27180840/131900493-94ca6dc6-f5eb-48fa-8e06-ca8752e00c20.png)

### 이벤트스토밍 - 완성된 모형

![image](https://user-images.githubusercontent.com/27180840/131900964-1a339a63-0b18-426b-81cd-cbefaca5fa35.png)

### 이벤트스토밍 - 기능 요구사항 Coverage Check

![image](https://user-images.githubusercontent.com/27180840/131900771-d970985c-dbec-484f-b9a5-f3880fa5570a.png)

### 이벤트스토밍 - 비기능 요구사항 Coverage Check

![image](https://user-images.githubusercontent.com/27180840/131901431-b7a38c42-6ad9-471a-818f-937e7f3ab372.png)

   1. 고객은 결제가 완료되어야 배송 서비스 이용이 가능하다. (Sync)
   2. 과일 주문 서비스는 24시간 이용이 가능하다 (Async 호출-event-driven)
   3. 결제 서비스가 과중되면 잠시 후에 하도록 유도한다. (Circuit breaker, fallback)
   4. 고객이 주문한 내역의 진행현황을 확인할 수 있다. (CQRS)
   5. 주문/결제상태가 바뀔 때 마다 MyPage에서 상태를 확인할 수 있다.(Event Driven)

### 헥사고날 아키텍처

![image](https://user-images.githubusercontent.com/27180840/131903383-a74d7f5d-58c9-4071-9a1b-5373c04a5094.png)


## 구현

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 
구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (서비스 포트는 8081, 8082, 8083, 8088 이다)

@@@@@@@ 포트 확인 필요!!!! 

```
cd order
mvn spring-boot:run

cd payment
mvn spring-boot:run

cd delivery
mvn spring-boot:run

cd gateway
mvn spring-boot:run

cd mypage
mvn spring-boot:run

```
*****

### DDD의 적용

1. 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. 
(예시는 order 마이크로 서비스 )

#### order.java

```
package fruitsorenew;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long orderId;
    private Long userId;
    private String prodNm;
    private Integer qty;
    private Integer price;
    private String address;
    private String orderStatus;

    @PostPersist
    public void onPostPersist(){
        OrderPlaced orderPlaced = new OrderPlaced();
        BeanUtils.copyProperties(this, orderPlaced);
        orderPlaced.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        fruitsorenew.external.Payment payment = new fruitsorenew.external.Payment();
        // mappings goes here
        payment.setOrderId(this.getOrderId());
        payment.setUserId(this.getUserId());
        payment.setPrice(this.getPrice());
        payment.setPayStatus("결제요청");
        OrderApplication.applicationContext.getBean(fruitsorenew.external.PaymentService.class)
            .payment(payment);
    }

    @PostRemove
    public void onPostRemove(){
        OrderCanceled orderCanceled = new OrderCanceled();
        BeanUtils.copyProperties(this, orderCanceled);
        orderCanceled.publishAfterCommit();
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getProdNm() {
        return prodNm;
    }
    ...
```

2. Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 
데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 
자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다

#### OrderRepository.java

```
package fruitsorenew;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="orders", path="orders")
public interface OrderRepository extends PagingAndSortingRepository<Order, Long>{
}

```

3. 적용 후 REST API 의 테스트

#### request 서비스의 요청처리

```
http order:8080/orders orderId="2" userId="7181" prodNm="사과" qty=2 price=5000  address="경기도 성남시" orderStatus="주문신청"  

```


#### 요청상태 확인

```
http http://order:8080/orders/107

@ root@siege:/# http order:8080/orders/107
HTTP/1.1 201 
Content-Type: application/json;charset=UTF-8
Date: Fri, 03 Sep 2021 02:01:31 GMT
Location: http://order:8080/orders/107
Transfer-Encoding: chunked

{
    "_links": {
        "order": {
            "href": "http://order:8080/orders/107"
        },
        "self": {
            "href": "http://order:8080/orders/107"
        }
    },
    "address": "경기도 성남시",
    "orderStatus": "주문신청",
    "price": 50000,
    "prodId": 200,
    "qty": 5,
    "userId": 7181
}

@ root@siege:/# http payment:8080/payments/17
HTTP/1.1 200 
Content-Type: application/hal+json;charset=UTF-8
Date: Fri, 03 Sep 2021 02:23:22 GMT
Transfer-Encoding: chunked

{
    "_links": {
        "payment": {
            "href": "http://payment:8080/payments/17"
        },
        "self": {
            "href": "http://payment:8080/payments/17"
        }
    },
    "orderId": 5,
    "orderStatus": null,
    "payStatus": "결제요청",
    "price": 50000,
    "prodId": null,
    "qty": null,
    "userId": 7181
}

@http://delivery:8080/deliveries/38

HTTP/1.1 201 
Content-Type: application/json;charset=UTF-8
Date: Fri, 03 Sep 2021 02:30:45 GMT
Location: http://delivery:8080/deliveries/38
Transfer-Encoding: chunked

{
    "_links": {
        "delivery": {
            "href": "http://delivery:8080/deliveries/38"
        },
        "self": {
            "href": "http://delivery:8080/deliveries/38"
        }
    },
    "address": "경기도 성남시",
    "deliveryStatus": "배송시작",
    "orderId": 5,
    "orderStatus": null,
    "payStatus": null,
    "prodId": null,
    "qty": 5,
    "userId": 7181
}
```
*****


### 동기식 호출과 Fallback 처리

분석단계에서의 조건 중 하나로 주문(order)-> 결제(payment) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리한다. 
호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출한다.

1. 결제서비스를 호출하기 위하여 FeignClient 를 이용하여 Service 대행 인터페이스 구현

#### PaymentService.java

```
@FeignClient(name="payment", url="http://localhost:8085", fallback = PaymentServiceFallback.class)
public interface PaymentService {
    @RequestMapping(method= RequestMethod.GET, path="/payments")
    public void payment(@RequestBody Payment payment);
}
```

2. 주문 생성 직후(@PostPersist) 결제를 요청하도록 처리 Order.java Entity Class 내 추가 (Correlation)

#### Order.java

```
package fruitsorenew;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long orderId;
    private Long userId;
    private String prodNm;
    private Integer qty;
    private Integer price;
    private String address;
    private String orderStatus;

    @PostPersist
    public void onPostPersist(){
        OrderPlaced orderPlaced = new OrderPlaced();
        BeanUtils.copyProperties(this, orderPlaced);
        orderPlaced.publishAfterCommit();

        fruitsorenew.external.Payment payment = new fruitsorenew.external.Payment();
        payment.setOrderId(this.getOrderId());
        payment.setUserId(this.getUserId());
        payment.setPrice(this.getPrice());
        payment.setPayStatus("결제요청");
        OrderApplication.applicationContext.getBean(fruitsorenew.external.PaymentService.class)
            .payment(payment);
    }

    @PostRemove
    public void onPostRemove(){
        OrderCanceled orderCanceled = new OrderCanceled();
        BeanUtils.copyProperties(this, orderCanceled);
        orderCanceled.publishAfterCommit();
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getProdNm() {
        return prodNm;
    }

```
*****

### CQRS
과일 주문 내역 및 Status 에 대하여 고객(Customer)이 조회 할 수 있도록 CQRS 로 구현하였다.
order, payment, delivery 의 개별 Aggregate Status 를 통합 조회하여 성능 Issue 를 사전에 예방할 수 있다.
비동기식으로 처리되어 발행된 이벤트 기반 Kafka 를 통해 수신/처리 되어 별도 Table 에 관리한다

```

@Service
public class MyPageViewHandler {


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
            myPage.setUserId(orderPlaced.getUserId());
            myPage.setProdNm(orderPlaced.getProdNm());
            myPage.setQty(orderPlaced.getQty());
            myPage.setPrice(orderPlaced.getPrice());
            myPage.setOrderStatus(orderPlaced.getOrderStatus());
            // view 레파지 토리에 save
            myPageRepository.save(myPage);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentApproved_then_CREATE_2 (@Payload PaymentApproved paymentApproved) {
        try {

            if (!paymentApproved.validate()) return;

            // view 객체 생성
            MyPage myPage = new MyPage();
            // view 객체에 이벤트의 Value 를 set 함
            myPage.setOrderId(paymentApproved.getOrderId());
            myPage.setPayId(paymentApproved.getPayId());
            myPage.setPayStatus(paymentApproved.getPayStatus());
            // view 레파지 토리에 save
            myPageRepository.save(myPage);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentCanceled_then_UPDATE_1(@Payload PaymentCanceled paymentCanceled) {
        try {
            if (!paymentCanceled.validate()) return;
                // view 객체 조회
            Optional<MyPage> myPageOptional = myPageRepository.findByOrderId(paymentCanceled.getOrderId());

            if( myPageOptional.isPresent()) {
                 MyPage myPage = myPageOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 myPage.setPayStatus(paymentCanceled.getPayStatus());
                // view 레파지 토리에 save
                 myPageRepository.save(myPage);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCanceled_then_UPDATE_2(@Payload OrderCanceled orderCanceled) {
        try {
            if (!orderCanceled.validate()) return;
                // view 객체 조회
            Optional<MyPage> myPageOptional = myPageRepository.findByOrderId(orderCanceled.getOrderId());

            if( myPageOptional.isPresent()) {
                 MyPage myPage = myPageOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 myPage.setOrderStatus(orderCanceled.getOrderStatus());
                // view 레파지 토리에 save
                 myPageRepository.save(myPage);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

}


```

배송 서비스에서는 결제승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다

#### PolicyHandler.java

```
package fruitsorenew;

import fruitsorenew.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired DeliveryRepository deliveryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_Acceptdelivery(@Payload PaymentApproved paymentApproved){

        if(!paymentApproved.validate()) return;

        System.out.println("\n\n##### listener Acceptdelivery : " + paymentApproved.toJson() + "\n\n");

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCanceled_CancleDelivery(@Payload PaymentCanceled paymentCanceled){

        if(!paymentCanceled.validate()) return;

        System.out.println("\n\n##### listener CancleDelivery : " + paymentCanceled.toJson() + "\n\n");


    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
```
*****

## 운영
*****

### 서킷 브레이킹

1. Spring FeignClient + Hystrix 옵션을 사용하여 구현

2. 주문-걸제시 Request/Response 로 연동하여 구현이 되어있으며 요청이 과도할 경우 CB를 통하여 장애격리 

3. Hystrix 를 설정: 요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 

CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정

#### application.yml

```
feign:
  hystrix:
    enabled: true

hystrix:
  command:
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610
        
```

4. 결제 서비스의 임의 부하 처리 

#### Payment.java (Entity)

```
    @PostPersist
    public void onPostPersist(){
     ...
		try {
		    Thread.currentThread().sleep((long) (400 + Math.random() * 220));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
```

5. 부하테스터 seige 툴을 통한 서킷 브레이커 동작 확인
( 동시사용자 100명, 90초간 진행 )

root@siege:/# siege -v -c100 -t90S -r10 --content-type "application/json" 'http://request:8080/orders POST {"orderId"="001", "prodNm"="수박"}'

```
&&&&결과 넣기


HTTP/1.1 201     4.24 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     4.29 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 500     4.32 secs:     250 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     4.18 secs:     370 bytes ==> POST http://request:8080/requests

Lifting the server siege...
Transactions:                   1545 hits
Availability:                  71.40 %
Elapsed time:                  89.88 secs
Data transferred:               0.69 MB
Response time:                  5.66 secs
Transaction rate:              17.19 trans/sec
Throughput:                     0.01 MB/sec
Concurrency:                   97.34
Successful transactions:        1545
Failed transactions:             619
Longest transaction:           11.60
Shortest transaction:           0.01
```
운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 
동적 Scale out (replica의 자동적 추가,HPA) 을 통하여 시스템을 확장 해주는 후속처리가 필요.
*****

### 오토스케일 아웃
@@@@@@@ 이 부분 테스트 필요

#### 앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다.
결제서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 
설정은 CPU 사용량이 50프로를 넘어서면 replica 를 10개까지 늘려준다

```
@@@@@@ 경로 명령어 수정 필요

root@labs-:/home/project/fruitstore/order# kubectl autoscale deployment order --cpu-percent=50 --min=1 --max=10
horizontalpodautoscaler.autoscaling/request autoscaled
```

#### 부하 테스트 진행

root@siege:/# siege -v -c100 -t90S -r10 --content-type "application/json" 'http://request:8080/requests POST {"accountNo":"1111","requestId":"01","requestName":"Deposit","amountOfMoney":10000,"userId":"1@sk.com","userName":"sam","userPassword":"1234"}'
( 동시사용자 100명, 90초간 진행 )

```
HTTP/1.1 201     1.63 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.98 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.80 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.09 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.90 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.89 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.18 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.19 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     1.71 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.10 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.80 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.81 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.82 secs:     370 bytes ==> POST http://request:8080/requests

Lifting the server siege...
Transactions:                   8988 hits
Availability:                 100.00 %
Elapsed time:                  89.51 secs
Data transferred:               3.17 MB
Response time:                  0.99 secs
Transaction rate:             100.41 trans/sec
Throughput:                     0.04 MB/sec
Concurrency:                   99.42
Successful transactions:        8989
Failed transactions:               0
Longest transaction:            8.59
Shortest transaction:           0.01
```

#### Terminal 을 추가하여 오토스케일링 현황을 모니터링 한다. ( watch kubectl get pod )

#### 부하 테스트 진행전 

```
Every 2.0s: kubectl get pod       labs-579721623: Thu Aug 19 08:44:47 2021

NAME                              READY   STATUS    RESTARTS   AGE
account-6b844c4f44-gdsvd          1/1     Running   0          145m
auth-7c55b8b7b9-9r6bb             1/1     Running   0          145m
efs-provisioner-fbcc88cb8-zrlzx   1/1     Running   0          79m
gateway-55bd75dfb9-cwlvg          1/1     Running   0          142m
history-77cc54b895-v5nqm          1/1     Running   0          144m
mypage-7bc648bd4d-5psgz           1/1     Running   0          143m
request-675f455d5c-7txbc          1/1     Running   0          28m
```

#### 부하 테스트 진행 후 

```
Every 2.0s: kubectl get pod       labs-579721623: Thu Aug 19 08:46:34 2021

NAME                              READY   STATUS    RESTARTS   AGE
account-6b844c4f44-gdsvd          1/1     Running   0          147m
auth-7c55b8b7b9-9r6bb             1/1     Running   0          147m
efs-provisioner-fbcc88cb8-zrlzx   1/1     Running   0          81m
gateway-55bd75dfb9-cwlvg          1/1     Running   0          144m
history-77cc54b895-v5nqm          1/1     Running   0          146m
mypage-7bc648bd4d-5psgz           1/1     Running   0          144m
request-675f455d5c-256tz          0/1     Running   0          31s
request-675f455d5c-6s2nz          0/1     Running   0          46s
request-675f455d5c-7txbc          1/1     Running   0          30m
request-675f455d5c-bz4nq          0/1     Running   0          46s
request-675f455d5c-mdbbl          0/1     Running   0          46s
siege                             1/1     Running   0          3h19m
```

#### 부하테스트 결과 Availability 는 100% 를 보이며 성공하였고, 늘어난 pod 개수를 통하여

오토 스케일링이 정상적으로 수행되었음을 확인할 수 있다. 
*****

### 무정지 재배포

#### 무정지 재배포 여부를 확인을 위해서 Autoscaler 와 CB 설정을 제거한다.

root@siege:/# siege -v -c100 -t90S -r10 --content-type "application/json" 'http://request:8080/requests POST {"accountNo":"1111","requestId":"01","requestName":"Deposit","amountOfMoney":10000,"userId":"1@sk.com","userName":"sam","userPassword":"1234"}'
( 동시사용자 100명, 90초간 진행 )

#### 부하테스트중 추가 생성한 Terminal 에서 readiness 설정되지 않은 버젼으로 재배포 한다.

```
root@labs-579721623:/home/project/online-bank/yaml# kubectl apply -f request-redeploy.yaml
deployment.apps/request configured
service/request configured

[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
[error] socket: unable to connect sock.c:249: Connection refused
HTTP/1.1 201     0.83 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.86 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.84 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     1.50 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.94 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.91 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.94 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     1.64 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.81 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     1.43 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.88 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.93 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     1.40 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.89 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     1.42 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.94 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.85 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.85 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.90 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.93 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.80 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.92 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.94 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.94 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.97 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.97 secs:     370 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.91 secs:     370 bytes ==> POST http://request:8080/requests
siege aborted due to excessive socket failure; you
can change the failure threshold in $HOME/.siegerc

Transactions:                   1154 hits
Availability:                  51.38 %
Elapsed time:                  12.57 secs
Data transferred:               0.41 MB
Response time:                  1.06 secs
Transaction rate:              91.81 trans/sec
Throughput:                     0.03 MB/sec
Concurrency:                   97.58
Successful transactions:        1154
Failed transactions:            1092
Longest transaction:            4.69
Shortest transaction:           0.02
```

#### 부하테스트중 새로 배포된 서비스를 READY 상태로 인지하여 서비스 중단됨을 확인함.

#### 부하테스트 진행

root@siege:/# siege -v -c100 -t30S -r10 --content-type "application/json" 'http://request:8080/requests POST {"accountNo":"1111","requestId":"01","requestName":"Deposit","amountOfMoney":10000,"userId":"1@sk.com","userName":"sam","userPassword":"1234"}'
( 동시사용자 100명, 90초간 진행 )

#### 부하테스트중 추가 생성한 Terminal 에서 readiness 설정 되어있는 버젼으로 재배포 한다.

```
root@labs-579721623:/home/project/online-bank/yaml# kubectl apply -f request-deploy.yaml
deployment.apps/request configured
service/request unchanged

HTTP/1.1 201     0.37 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.56 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.38 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.38 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.36 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.35 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.36 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.34 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.36 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.37 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.35 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.05 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.33 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.05 secs:     372 bytes ==> POST http://request:8080/requests
HTTP/1.1 201     0.08 secs:     372 bytes ==> POST http://request:8080/requests

Lifting the server siege...
Transactions:                  24451 hits
Availability:                 100.00 %
Elapsed time:                  89.32 secs
Data transferred:               8.65 MB
Response time:                  0.36 secs
Transaction rate:             273.75 trans/sec
Throughput:                     0.10 MB/sec
Concurrency:                   99.31
Successful transactions:       24451
Failed transactions:               0
Longest transaction:            2.72
Shortest transaction:           0.00
```

#### 배포중 Availability 100%를 보이며 무정지 재배포가 정상적으로 성공하였다.
*****

### Gateway / Corelation

#### Gateway 기능이 정상적으로 수행되는지 확인하기 위하여 Gateway를 통하여 요청서비스를 호출한다.  

@application.yml 파일 (gateway 서비스)

```
   cloud:
    gateway:
      routes:
        - id: order
          #uri: http://localhost:8086
          uri: http://localhost:8081
          predicates:
            - Path=/orders/** 
        - id: mypage
          uri: http://localhost:8082
          predicates:
            - Path= /myPages/**
        - id: payment
          #uri: http://localhost:8083
          uri: http://localhost:8087
          predicates:
            - Path=/payments/** 
        - id: delivery
          uri: http://localhost:8084
          predicates:
            - Path=/deliveries/** 
```	    

게이트웨이 포트를 활용하여 각 서비스로 접근 가능한지 확인

게이트웨이포트 8080 을 통해서 8081포트에 서비스하고 있는 주문(Order)서비스를 접근함

??????? 이건 가능한지 나중에 확인해봐야함 로그 변경필요

```
root@siege:/# http gateway:8080/requests accountNo="1111" requestId="01" requestName="Deposit" amountOfMoney=10000 userId="1@sk.com" userName="sam" userPassword="1234"

HTTP/1.1 201 Created
Content-Type: application/json;charset=UTF-8
Date: Thu, 19 Aug 2021 06:54:01 GMT
Location: http://request:8080/requests/2
transfer-encoding: chunked

{
    "_links": {
        "request": {
            "href": "http://request:8080/requests/2"
        },
        "self": {
            "href": "http://request:8080/requests/2"
        }
    },
    "accountNo": "1111",
    "amountOfMoney": 10000,
    "requestDate": null,
    "requestId": "01",
    "requestName": "Deposit",
    "userId": "1@sk.com",
    "userName": "sam",
    "userPassword": "1234"
}
```
#### 요청 처리결과를 통하여 Gateway 기능이 정상적으로 수행되었음을 확인할 수 있다. 

*****

### 동기식 호출 (운영)

#### 동기식 호출인 관계로 결제서비스 장애시 서비스를 처리할 수 없다. 

1) 결제 서비스 임시로 삭제한다. 

```
@@@@@ 소스 수정로그 수정 필요 , 경로랑 잘 확인하기,, yaml은 왜? 
root@labs-579721623:/home/project/fruitstore/yaml# kubectl delete service auth
service "auth" deleted
```

2) 요청 처리결과를 확인한다.
********************오늘 데이터 입력 안된거,, 꼭 다시 해보기 

```
root@siege:/# http request:8080/requests accountNo="1111" requestId="01" requestName="Deposit" amountOfMoney=10000 userId="1@sk.com" userName="sam" userPassword="1234"
HTTP/1.1 500 
Connection: close
Content-Type: application/json;charset=UTF-8
Date: Thu, 19 Aug 2021 06:59:08 GMT
Transfer-Encoding: chunked

{
    "error": "Internal Server Error",
    "message": "Could not commit JPA transaction; nested exception is javax.persistence.RollbackException: Error while committing the transaction",
    "path": "/requests",
    "status": 500,
    "timestamp": "2021-08-19T06:59:08.624+0000"
}
```

3) 인증서비스 재기동 한다. 

```
root@labs-579721623:/home/project/online-bank/yaml# kubectl expose deploy auth --type="LoadBalancer" --port=8080
service/auth exposed
```

4) 요청처리 결과를 확인한다. 

```
root@siege:/# http http://request:8080/requests accountNo="1111" requestId="01" requestName="Deposit" amountOfMoney=10000 userId="1@sk.com" userName="sam" userPassword="1234"

HTTP/1.1 201 
Content-Type: application/json;charset=UTF-8
Date: Thu, 19 Aug 2021 07:02:31 GMT
Location: http://request:8080/requests/4
Transfer-Encoding: chunked

{
    "_links": {
        "request": {
            "href": "http://request:8080/requests/4"
        },
        "self": {
            "href": "http://request:8080/requests/4"
        }
    },
    "accountNo": "1111",
    "amountOfMoney": 10000,
    "requestDate": null,
    "requestId": "01",
    "requestName": "Deposit",
    "userId": "1@sk.com",
    "userName": "sam",
    "userPassword": "1234"
}
```

#### 테스트를 통하여 결제 서비스가 기동되지 않은 상태에서는 주문 요청이 실패함을 확인 할 수 있음.
*****


### Persistence Volume

#### Persistence Volume 을 생성한다. 

```
root@labs-579721623:/home/project/online-bank/yaml# kubectl get pv

NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                      STORAGECLASS   REASON   AGE
pvc-60c0deaa-241e-443d-a770-2c4890b0d9db   1Gi        RWO            Delete           Bound    kafka/datadir-my-kafka-2   gp2                     174m
pvc-ce2fe4aa-be29-4c82-8637-7d247b243456   1Gi        RWO            Delete           Bound    kafka/datadir-my-kafka-1   gp2                     175m
pvc-f0331c5b-0127-475f-93db-58999bb38980   1Gi        RWO            Delete           Bound    kafka/datadir-my-kafka-0   gp2                     177m
task-pv-volume                             100Mi      RWO            Retain           Bound    labs-579721623/aws-efs     aws-efs                 4m4s
```

#### Persistence Volume Claim 을 생성한다. 

```
root@labs-579721623:/home/project/online-bank/yaml# kubectl get pvc

NAME      STATUS   VOLUME           CAPACITY   ACCESS MODES   STORAGECLASS   AGE
aws-efs   Bound    task-pv-volume   100Mi      RWO            aws-efs        101s
```

#### Pod 로 접속하여 파일시스템 정보를 확인한다. 

```
root@labs-579721623:/home/project/online-bank/yaml# kubectl get pod

NAME                              READY   STATUS             RESTARTS   AGE
account-6b844c4f44-gdsvd          1/1     Running            0          76m
auth-7c55b8b7b9-9r6bb             1/1     Running            0          76m
efs-provisioner-fbcc88cb8-zrlzx   1/1     Running            0          10m
gateway-55bd75dfb9-cwlvg          1/1     Running            0          73m
history-77cc54b895-v5nqm          1/1     Running            0          75m
mypage-7bc648bd4d-5psgz           1/1     Running            0          73m
request-5cdc6474bf-p76tr          0/1     ImagePullBackOff   0          25m
request-646c4cc7c6-xmk59          1/1     Running            0          28m
siege                             1/1     Running            0          128m

root@labs-579721623:/home/project/online-bank/yaml# kubectl exec -it request-646c4cc7c6-xmk59 -- /bin/bash
```

#### 생성된 Persistence Volume 은 Mount 되지 않은 상태임을 확인한다. 

```
root@request-646c4cc7c6-xmk59:/# df -h
Filesystem      Size  Used Avail Use% Mounted on
overlay          80G  4.2G   76G   6% /
tmpfs            64M     0   64M   0% /dev
tmpfs           1.9G     0  1.9G   0% /sys/fs/cgroup
/dev/nvme0n1p1   80G  4.2G   76G   6% /etc/hosts
shm              64M     0   64M   0% /dev/shm
tmpfs           1.9G   12K  1.9G   1% /run/secrets/kubernetes.io/serviceaccount
tmpfs           1.9G     0  1.9G   0% /proc/acpi
tmpfs           1.9G     0  1.9G   0% /sys/firmware
```

#### Persistenct Volume 이 Mount 되도록 yaml 설정파일을 변경한다. 

#### request-deploy-vol.yaml

```
    spec:
      containers:
        - name: request
          image: 879772956301.dkr.ecr.ap-northeast-2.amazonaws.com/request
          imagePullPolicy: Always
          ports:
            - containerPort: 8080
...
          volumeMounts:
          - mountPath: "/mnt/aws"
            name: volume
...
      volumes:
        - name: volume
          persistentVolumeClaim:
            claimName: aws-efs
```

#### 변경된 yaml 파일로 서비스 재배포 한다. 

```
root@labs-579721623:/home/project/online-bank/yaml# kubectl apply -f request-deploy-vol.yaml
deployment.apps/request created
```

#### Pod 로 접속하여 파일시스템 정보를 확인한다. 

```
root@request-675f455d5c-t8lzd:/# df -h
Filesystem      Size  Used Avail Use% Mounted on
overlay          80G  4.1G   76G   6% /
tmpfs            64M     0   64M   0% /dev
tmpfs           1.9G     0  1.9G   0% /sys/fs/cgroup
/dev/nvme0n1p1   80G  4.1G   76G   6% /mnt/aws
shm              64M     0   64M   0% /dev/shm
tmpfs           1.9G   12K  1.9G   1% /run/secrets/kubernetes.io/serviceaccount
tmpfs           1.9G     0  1.9G   0% /proc/acpi
tmpfs           1.9G     0  1.9G   0% /sys/firmware
```

#### 생성된 Persistence Volume 이 pod 내 정상 mount 되었음을 확인할 수 있다. 
*****

### Liveness Prove

#### request 서비스 배포시 yaml 파일내 Liveness Prove 설정을 추가한다. 

#### request-deploy.yaml

```
spec:
  replicas: 1
  selector:
    matchLabels:
      app: request
  template:
    metadata:
      labels:
        app: request
    spec:
      containers:
        - name: request
          image: 879772956301.dkr.ecr.ap-northeast-2.amazonaws.com/request
          imagePullPolicy: Always
          ports:
            - containerPort: 8080
...
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
```

#### Liveness Prove 설정 정상 적용여부를 확인하기 위해서 기존에 생성된 request pod 삭제시

정상적으로 재생성 되는지 여부를 확인한다. 

```
root@labs-579721623:/home/project/online-bank/yaml# kubectl get pod
NAME                              READY   STATUS    RESTARTS   AGE
account-6b844c4f44-gdsvd          1/1     Running   0          84m
auth-7c55b8b7b9-9r6bb             1/1     Running   0          85m
efs-provisioner-fbcc88cb8-zrlzx   1/1     Running   0          19m
gateway-55bd75dfb9-cwlvg          1/1     Running   0          82m
history-77cc54b895-v5nqm          1/1     Running   0          84m
mypage-7bc648bd4d-5psgz           1/1     Running   0          82m
request-675f455d5c-t8lzd          1/1     Running   0          2m10s
siege                             1/1     Running   0          137m
```

#### request pod 를 삭제한다. 

```
root@labs-579721623:/home/project/online-bank/yaml# kubectl delete pod request-675f455d5c-t8lzd
pod "request-675f455d5c-t8lzd" deleted
```

#### request pod 삭제 후 pod 정보를 재조회 한다. 

```
root@labs-579721623:/home/project/online-bank/yaml# kubectl get pod
NAME                              READY   STATUS    RESTARTS   AGE
account-6b844c4f44-gdsvd          1/1     Running   0          85m
auth-7c55b8b7b9-9r6bb             1/1     Running   0          85m
efs-provisioner-fbcc88cb8-zrlzx   1/1     Running   0          19m
gateway-55bd75dfb9-cwlvg          1/1     Running   0          83m
history-77cc54b895-v5nqm          1/1     Running   0          85m
mypage-7bc648bd4d-5psgz           1/1     Running   0          83m
request-675f455d5c-zqhwq          0/1     Running   0          13s
siege                             1/1     Running   0          138m
```

#### request pod Liveness Prove 설정이 적용되어 삭제 후 다른 이름으로 재생성 되었음을 확인할 수 있다. 
