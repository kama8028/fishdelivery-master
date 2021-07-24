package fishdelivery;

import fishdelivery.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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


    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayApproved_then_UPDATE_1(@Payload PayApproved payApproved) {
        try {
            if (!payApproved.validate()) return;
                // view 객체 조회
            Optional<MyPage> myPageOptional = myPageRepository.findById(payApproved.getOrderId());

            if( myPageOptional.isPresent()) {
                 MyPage myPage = myPageOptional.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                 myPage.setOrderId(payApproved.getOrderId());
                 myPage.setStatus(payApproved.getStatus());
                // view 레파지 토리에 save
                 myPageRepository.save(myPage);
                }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderTaken_then_UPDATE_2(@Payload OrderTaken orderTaken) {
        try {
            if (!orderTaken.validate()) return;
            // view 객체 조회
            Optional<MyPage> myPageOptional = myPageRepository.findById(orderTaken.getOrderId());

            if( myPageOptional.isPresent()) {
                 MyPage myPage = myPageOptional.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                 myPage.setStatus(orderTaken.getStatus());
                // view 레파지 토리에 save
                 myPageRepository.save(myPage);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenRecieveCanceled_then_UPDATE_3(@Payload RecieveCanceled recieveCanceled) {
        try {
            if (!recieveCanceled.validate()) return;
                // view 객체 조회
               Optional<MyPage> myPageOptional = myPageRepository.findById(recieveCanceled.getOrderId());

                if( myPageOptional.isPresent()) {
                    MyPage myPage = myPageOptional.get();
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setStatus(recieveCanceled.getStatus());
                    // view 레파지 토리에 save
                    myPageRepository.save(myPage);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryStarted_then_UPDATE_4(@Payload DeliveryStarted deliveryStarted) {
        try {
            if (!deliveryStarted.validate()) return;
            // view 객체 조회
            Optional<MyPage> myPageOptional = myPageRepository.findById(deliveryStarted.getOrderId());

            if( myPageOptional.isPresent()) {
                MyPage myPage = myPageOptional.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                myPage.setStatus(deliveryStarted.getStatus());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCanceled_then_UPDATE_5(@Payload OrderCanceled orderCanceled) {
        try {
            if (!orderCanceled.validate()) return;
            // view 객체 조회
            Optional<MyPage> myPageOptional = myPageRepository.findById(orderCanceled.getOrderId());

            if( myPageOptional.isPresent()) {
                MyPage myPage = myPageOptional.get();
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                myPage.setStatus(orderCanceled.getStatus());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCanceled_then_DELETE_1(@Payload OrderCanceled orderCanceled) {
        try {
            if (!orderCanceled.validate()) return;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

