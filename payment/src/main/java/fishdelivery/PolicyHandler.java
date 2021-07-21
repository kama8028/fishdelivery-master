package fishdelivery;

import fishdelivery.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyHandler{
    @Autowired PaymentRepository paymentRepository;

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

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

}
