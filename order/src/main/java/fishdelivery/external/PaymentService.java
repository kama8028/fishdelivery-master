
package fishdelivery.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/* @FeignClient Netflix에서 만들어졌고 spring-cloud-starter-openfeign 으로 스프링 라이브러리에서 사용
fallback : hystrix fallback class 지정
spring-cloud의 서비스 중 하나. Circuit Breaker Pattern을 사용.
뒷단 API 서버가 장애 발생 등의 이유로 일정 시간(Time window) 내에 여러번 오류 응답을 주는 경우(timeout, bad gateway 등),
해당 API 서버로 요청을 보내지 않고 잠시 동안 대체(fallback) method를 실행. 일정 시간이 지나서 다시 뒷단 API 서버를 호출하는 등의, 일련의 작업을 제공
 */
//@FeignClient(name="payment", Process.value = "url=\"http://payment:8080\"", fallback = PaymentServiceFallback.class)
@FeignClient(name="payment", url="http://payment:8080")
//@FeignClient(name="payment", url="http://localhost:8082", fallback = PaymentServiceFallback.class)
public interface PaymentService {
    @RequestMapping(method= RequestMethod.POST, path="/payments")
    public void pay(@RequestBody Payment payment);
}

