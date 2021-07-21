package fishdelivery;

import fishdelivery.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired FishstoreRepository fishstoreRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayApproved_TakeOrder(@Payload PayApproved payApproved){

        if(!payApproved.validate()) return;

        System.out.println("\n\n##### listener TakeOder : " + payApproved.toJson() + "\n\n");

        /* 한용선 주문이 들어오면 DB에 저장을 한다. */
        Fishstore fishstore = new Fishstore();
        fishstore.setOrderId(payApproved.getOrderId());
        fishstore.setCustomerName(payApproved.getCustomerName());
        fishstore.setFishName(payApproved.getFishName());
        fishstore.setQty(payApproved.getQty());
        fishstore.setTelephone(payApproved.getTelephone());
        fishstore.setAddress(payApproved.getAddress());
        fishstore.setStatus(payApproved.getStatus());
        fishstoreRepository.save(fishstore);
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_CancelOrder(@Payload OrderCanceled orderCanceled){

        if(!orderCanceled.validate()) return;

        System.out.println("\n\n##### listener CancelOrder : " + orderCanceled.toJson() + "\n\n");

        Fishstore fishstore = fishstoreRepository.findByOrderId(orderCanceled.getOrderId());
        fishstore.setStatus(orderCanceled.getStatus());
        fishstoreRepository.save(fishstore);

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
