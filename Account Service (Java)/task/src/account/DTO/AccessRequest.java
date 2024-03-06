package account.DTO;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AccessRequest {
    String user;
    @Pattern(regexp = "LOCK|UNLOCK")
    String operation;

    public Boolean isLocked() {
        return "LOCK".equals(operation);
    }
}