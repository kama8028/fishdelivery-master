package fishdelivery;

import javax.persistence.*;

import fishdelivery.external.PaymentService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

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

        fishdelivery.external.Payment payment = new fishdelivery.external.Payment();
        payment.setOrderId(orderPlaced.getOrderId());
        payment.setCustomerName(orderPlaced.getCustomerName());
        payment.setFishName(orderPlaced.getFishName());
        payment.setQty(orderPlaced.getQty());
        payment.setTelephone(orderPlaced.getTelephone());
        payment.setAddress(orderPlaced.getAddress());
        payment.setStatus(orderPlaced.getStatus());

        GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
        ConfigurableEnvironment env = ctx.getEnvironment();
        System.out.println("한용선 로그" + env.getProperty("URL") + "${URL}");

        OrderApplication.applicationContext.getBean(fishdelivery.external.PaymentService.class).pay(payment);
    }
    @PostUpdate
    public void onPostUpdate(){
        OrderCanceled orderCanceled = new OrderCanceled();
        BeanUtils.copyProperties(this, orderCanceled);
        orderCanceled.publishAfterCommit();
    }
}
