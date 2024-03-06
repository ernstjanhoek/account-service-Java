package account.DTO;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class StatusResponseUser {
    @NonNull
    private String user;
    @NonNull
    private String status;
}