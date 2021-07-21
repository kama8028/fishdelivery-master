package fishdelivery;

import fishdelivery.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired OrderRepository orderRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_ChangeStatus(@Payload DeliveryStarted deliveryStarted){

        if(!deliveryStarted.validate()) return;

        System.out.println("\n\n##### listener ChangeStatus : " + deliveryStarted.toJson() + "\n\n");



        // Sample Logic //
        // Order order = new Order();
        // orderRepository.save(order);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayApproved_ChangeStatus(@Payload PayApproved payApproved){

        if(!payApproved.validate()) return;

        System.out.println("\n\n##### listener ChangeStatus : " + payApproved.toJson() + "\n\n");



        // Sample Logic //
        // Order order = new Order();
        // orderRepository.save(order);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRecieveCanceled_ChangeStatus(@Payload RecieveCanceled recieveCanceled){

        if(!recieveCanceled.validate()) return;

        System.out.println("\n\n##### listener ChangeStatus : " + recieveCanceled.toJson() + "\n\n");



        // Sample Logic //
        // Order order = new Order();
        // orderRepository.save(order);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderRecieved_ChangeStatus(@Payload OrderTaken orderTaken){

        if(!orderTaken.validate()) return;

        System.out.println("\n\n##### listener ChangeStatus : " + orderTaken.toJson() + "\n\n");



        // Sample Logic //
        // Order order = new Order();
        // orderRepository.save(order);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCanceled_ChangeStatus(@Payload PayCanceled payCanceled){

        if(!payCanceled.validate()) return;

        System.out.println("\n\n##### listener ChangeStatus : " + payCanceled.toJson() + "\n\n");



        // Sample Logic //
        // Order order = new Order();
        // orderRepository.save(order);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
