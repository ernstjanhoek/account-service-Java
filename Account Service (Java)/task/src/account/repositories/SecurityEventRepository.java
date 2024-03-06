package account.repositories;

import account.Entities.SecurityEvent;
import org.springframework.data.repository.CrudRepository;

public interface SecurityEventRepository extends CrudRepository<SecurityEvent, Integer> {

}
