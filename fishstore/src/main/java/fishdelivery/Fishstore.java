package fishdelivery;

import javax.persistence.*;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;


@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name="Fishstore_table")
public class Fishstore {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long fishOrderId;
    private Long orderId;
    private String customerName;
    private String fishName;
    private Integer qty;
    private Integer telephone;
    private String address;
    private String status;

    @PostPersist
    public void onPostPersist(){
        if(this.getStatus().equals("prepared")) {
            OrderTaken orderTaken = new OrderTaken();
            BeanUtils.copyProperties(this, orderTaken);
            orderTaken.publishAfterCommit();
        }
    }
    @PostUpdate
    public void onPostUpdate(){
        if(this.getStatus().equals("prepared")) {
            OrderTaken orderTaken = new OrderTaken();
            BeanUtils.copyProperties(this, orderTaken);
            orderTaken.publishAfterCommit();
        }
        else if(this.getStatus().equals("canceled")) {
            RecieveCanceled recieveCanceled = new RecieveCanceled();
            BeanUtils.copyProperties(this, recieveCanceled);
            recieveCanceled.publishAfterCommit();
        }
    }
    @PrePersist
    public void onPrePersist(){
    }

}
