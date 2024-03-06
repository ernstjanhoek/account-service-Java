package account.repositories;
import account.usermanager.User;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer> {
    Optional<User> findUserByEmail(String username);
    boolean existsByEmail(String username);
    Optional<Long> findByEmail(String username);
    default void updateUserByEmail(String email, User updatedUser) {
        findUserByEmail(email).ifPresent(existingUser -> {
            existingUser.setPassword(updatedUser.getPassword());
            save(existingUser);
        });
    }
}