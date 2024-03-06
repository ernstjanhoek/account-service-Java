package account.repositories;

import account.Entities.Salary;
import account.Entities.SalaryID;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.CrudRepository;

public interface SalaryRepository extends CrudRepository<Salary, SalaryID> {
    Iterable<Salary> findAllByUserIdOrderByPeriodDesc(@NotNull Long userId);
}