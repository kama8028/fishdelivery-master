package fishdelivery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
 public class FishstoreController {
     @Autowired
     FishstoreRepository fishstoreRepository;

     /* 한용선 상점주인이 주문 접소를 처리하는 Command paid -> prepared로 update 한다 */
     @PatchMapping("/fishstores/{fishOrderId}")
     public void orderTaken(@PathVariable(value = "fishOrderId") Long fishOrderId){
         fishstoreRepository.findById(fishOrderId).ifPresent(fishstore->{
             fishstore.setStatus("prepared");
             fishstoreRepository.save(fishstore);
         });
     }
 }
