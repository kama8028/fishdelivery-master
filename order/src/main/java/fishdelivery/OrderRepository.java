package fishdelivery;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


// PagingAndSortingRepository<엔티티의 클래스 타입, 식별자타입(@Id로 맵핑한 식별자 변수의 타입)으로 선언한다.
@RepositoryRestResource(collectionResourceRel="orders", path="orders")
public interface OrderRepository extends PagingAndSortingRepository<Order, Long>{


}
