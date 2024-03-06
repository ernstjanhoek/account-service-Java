package account.usermanager;


import account.Entities.AccountGroups;
import account.Entities.Salary;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "service_users")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class User {
    @Id
    @GeneratedValue
    @NotNull
    private Long id;
    @Email
    @Column(unique = true)
    @NonNull
    private String email;
    @NonNull
    private String name;
    @NonNull
    private String lastname;
    @NonNull
    private String password;
    @NonNull
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinTable(
            uniqueConstraints = {
                   @UniqueConstraint(name = "UniqueIdClass", columnNames = {"user_id", "role_id"})
            },
            name = "user_group",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "role")
    )
    private Collection<AccountGroups> roles;
    @OneToMany(mappedBy = "salary")
    private Collection<Salary> salaries = new ArrayList<>();
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean locked = false;
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer failedLoginAttempts = 0;

    public void switchState() {
        this.locked = !locked;
    }

    public void incrementFailedLogin() {
        this.failedLoginAttempts++;
    }
}