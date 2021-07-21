package fishdelivery;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel="payments", path="payments")
public interface PaymentRepository extends PagingAndSortingRepository<Payment, Long>{
    /* 한용선 쿼리 메소드를 이용하여 검색 find + 엔티티이름(생략가능) + By + 변수 이름 */
    Payment findByOrderId(long orderId);
}
