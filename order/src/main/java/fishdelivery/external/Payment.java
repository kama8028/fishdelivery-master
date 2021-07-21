package fishdelivery.external;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Payment {

    private Long paymentId;
    private Long orderId;
    private String customerName;
    private String fishName;
    private Integer qty;
    private Integer telephone;
    private String address;
    private String status;

}
