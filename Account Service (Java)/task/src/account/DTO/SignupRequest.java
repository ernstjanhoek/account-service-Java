package account.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank(message = "Empty name field!")
    private String name;
    @NotBlank(message = "Empty lastname field!")
    private String lastname;
    @Email
    @NotBlank
    @Pattern(regexp = ".*@acme\\.com$")
    private String email;
    @NotBlank
    @Pattern(regexp = ".{12,}", message = "Password length must be 12 chars minimum!" )
    private String password;
}
