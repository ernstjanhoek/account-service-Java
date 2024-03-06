package account;

import account.Entities.AccountGroups;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import account.repositories.AccountGroupsRepository;

@Component
public class DataLoader {

    private AccountGroupsRepository accountGroupsRepository;

    @Autowired
    public DataLoader(AccountGroupsRepository accountGroupsRepository) {
        this.accountGroupsRepository = accountGroupsRepository;
        createRoles();
    }

    private void createRoles() {
        try {
            AccountGroups role = new AccountGroups();
            role.setRole("ROLE_ADMINISTRATOR");
            accountGroupsRepository.save(role);
            accountGroupsRepository.save(new AccountGroups("ROLE_ACCOUNTANT"));
            accountGroupsRepository.save(new AccountGroups("ROLE_AUDITOR"));
            accountGroupsRepository.save(new AccountGroups("ROLE_USER"));
        } catch (Exception ignored) {}
    }
}