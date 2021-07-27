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
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long orderId;
    private String customerName;
    private String fishName;
    private Integer qty;
    private Integer telephone;
    private String address;
    private String status;

    @PostPersist
    public void onPostPersist(){
        OrderPlaced orderPlaced = new OrderPlaced();
        BeanUtils.copyProperties(this, orderPlaced);
        orderPlaced.publishAfterCommit();

        System.out.println(Process.class.getResource("URL"));

        fishdelivery.external.Payment payment = new fishdelivery.external.Payment();
        payment.setOrderId(orderPlaced.getOrderId());
        payment.setCustomerName(orderPlaced.getCustomerName());
        payment.setFishName(orderPlaced.getFishName());
        payment.setQty(orderPlaced.getQty());
        payment.setTelephone(orderPlaced.getTelephone());
        payment.setAddress(orderPlaced.getAddress());
        payment.setStatus(orderPlaced.getStatus());

        OrderApplication.applicationContext.getBean(fishdelivery.external.PaymentService.class).pay(payment);
    }
    @PostUpdate
    public void onPostUpdate(){
        OrderCanceled orderCanceled = new OrderCanceled();
        BeanUtils.copyProperties(this, orderCanceled);
        orderCanceled.publishAfterCommit();
    }
}
