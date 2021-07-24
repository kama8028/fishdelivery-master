package fishdelivery;

import javax.persistence.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long paymentId;
    private Long orderId;
    private String customerName;
    private String fishName;
    private Integer qty;
    private Integer telephone;
    private String address;
    private String status;

    /* 한용선 Entity에 insert 일어나기 전에 Circuit breaker 실행을 위해 sleep을 줬다. */
    @PrePersist
    public void onPrePersist(){
        try{
            System.out.println("sleep실행");
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @PostPersist
    public void onPostPersist(){
        /* 한용선 주문상태가 paid인 경우만 kafka 이벤트를 publish 한다. */
        System.out.println("한용선 PostPersist");
        if(this.getStatus().equals("paid")) {
            PayApproved payApproved = new PayApproved();
            BeanUtils.copyProperties(this, payApproved);
            payApproved.publishAfterCommit();
        }
    }

    @PostUpdate
    public void onPostUpdate() {
        if(this.getStatus().equals("paid")) {
            PayApproved payApproved = new PayApproved();
            BeanUtils.copyProperties(this, payApproved);
            payApproved.publishAfterCommit();
        }
        else if(this.getStatus().equals("canceled")) {
            PayCanceled payCanceled = new PayCanceled();
            BeanUtils.copyProperties(this, payCanceled);
            payCanceled.publishAfterCommit();
        }
    }
}
