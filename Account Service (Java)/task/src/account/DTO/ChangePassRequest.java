package account.DTO;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePassRequest {
    @JsonProperty("new_password")
    @Pattern(regexp = ".{12,}", message = "Password length must be 12 chars minimum!" )
    private String newPassword;
}