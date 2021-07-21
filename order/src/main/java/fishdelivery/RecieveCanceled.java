package fishdelivery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class RecieveCanceled extends AbstractEvent {

    private Long fishOderId;
    private Long orderId;
    private String status;

}

