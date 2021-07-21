package fishdelivery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name="MyPage_table")
public class MyPage {

        @Id
        private Long orderId;
        private String customerName;
        private String fishName;
        private Integer qty;
        private Integer telephone;
        private String address;
        private String status;
}
