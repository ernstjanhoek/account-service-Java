package account.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

 @Data
 @NoArgsConstructor
 @AllArgsConstructor
public class SalaryID implements Serializable {
    private Long user;
    private Date period;
}
