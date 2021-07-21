package fishdelivery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class PayCanceled extends AbstractEvent {

    private Long paymentId;
    private Long orderId;
    private String status;

}
