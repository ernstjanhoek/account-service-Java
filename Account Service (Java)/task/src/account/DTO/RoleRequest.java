package account.DTO;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RoleRequest {
    private String user;

    private String role;

    @Pattern(regexp = "GRANT|REMOVE")
    private String operation;

    public void setUser(String input) {
        this.user = input.toLowerCase();
    }
}



