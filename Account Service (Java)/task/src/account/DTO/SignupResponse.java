package account.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupResponse {
    private long id;
    private String name;
    private String lastname;
    private String email;
    private Collection<String> roles;
}