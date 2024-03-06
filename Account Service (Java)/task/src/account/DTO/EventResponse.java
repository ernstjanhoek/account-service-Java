package account.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String date;
    private String action;
    private String subject;
    private String object;
    private String path;
}
