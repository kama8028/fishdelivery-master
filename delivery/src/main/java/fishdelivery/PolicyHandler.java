package fishdelivery;

import fishdelivery.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired DeliveryRepository deliveryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderTaken_StartDelivery(@Payload OrderTaken orderTaken){

        if(!orderTaken.validate()) return;

        System.out.println("\n\n##### listener StartDelivery : " + orderTaken.toJson() + "\n\n");

        /* 한용선 주문이 들어오면 DB에 저장을 한다. */
        Delivery delivery = new Delivery();
        delivery.setOrderId(orderTaken.getOrderId());
        delivery.setCustomerName(orderTaken.getCustomerName());
        delivery.setFishName(orderTaken.getFishName());
        delivery.setQty(orderTaken.getQty());
        delivery.setTelephone(orderTaken.getTelephone());
        delivery.setAddress(orderTaken.getAddress());
        delivery.setStatus("delivered");
        deliveryRepository.save(delivery);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRecieveCanceled_CancelDelivery(@Payload RecieveCanceled recieveCanceled){

        if(!recieveCanceled.validate()) return;

        System.out.println("\n\n##### listener CancelDelivery : " + recieveCanceled.toJson() + "\n\n");

        Delivery delivery = deliveryRepository.findByOrderId(recieveCanceled.getOrderId());
        delivery.setStatus(recieveCanceled.getStatus());
        deliveryRepository.save(delivery);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_CancelDelivery(@Payload OrderCanceled orderCanceled){

        if(!orderCanceled.validate()) return;

        System.out.println("\n\n##### listener CancelDelivery : " + orderCanceled.toJson() + "\n\n");

        Delivery delivery = deliveryRepository.findByOrderId(orderCanceled.getOrderId());
        delivery.setStatus(orderCanceled.getStatus());
        deliveryRepository.save(delivery);
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
