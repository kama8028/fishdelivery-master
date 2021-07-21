package fishdelivery;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="fishstores", path="fishstores")
public interface FishstoreRepository extends PagingAndSortingRepository<Fishstore, Long >{
    Fishstore findByOrderId(long orderId);
}
