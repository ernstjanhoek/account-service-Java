package account.DTO;


import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class StatusResponse {
    @NonNull
    private String status;
}
