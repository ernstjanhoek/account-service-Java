package account.repositories;

import account.Entities.AccountGroups;
import org.springframework.data.repository.CrudRepository;

public interface AccountGroupsRepository extends CrudRepository<AccountGroups, String> {
}
