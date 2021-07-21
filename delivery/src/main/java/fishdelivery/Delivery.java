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
@Table(name="Delivery_table")
public class Delivery {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long deliveryId;
    private Long orderId;
    private String customerName;
    private String fishName;
    private Integer qty;
    private Integer telephone;
    private String address;
    private String status;

    @PostPersist
    public void onPostPersist(){
        DeliveryStarted deliveryStarted = new DeliveryStarted();
        BeanUtils.copyProperties(this, deliveryStarted);
        deliveryStarted.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        DeliveryCanceled deliveryCanceled = new DeliveryCanceled();
        BeanUtils.copyProperties(this, deliveryCanceled);
        deliveryCanceled.publishAfterCommit();
    }
}
