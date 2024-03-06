package account.Entities;

import account.usermanager.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@Entity
@Data
@RequiredArgsConstructor
public class AccountGroups {
    @Id
    @NotNull
    @Column(unique = true)
    String role;
    public AccountGroups(String group) {
        this.role = group;
    }
}