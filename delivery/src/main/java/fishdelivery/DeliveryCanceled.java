package fishdelivery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class DeliveryCanceled extends AbstractEvent {

    private Long deliveryId;
    private Long orderId;
    private String status;

}
