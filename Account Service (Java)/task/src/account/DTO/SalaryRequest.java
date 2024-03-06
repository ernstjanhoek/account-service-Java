package account.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.validation.annotation.Validated;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@Data
public class SalaryRequest {
    @Min(value = 1, message = "salary must be greater or equal to 1") //, groups = {Default.class})
    private Long salary;
    @Email
    @NotBlank(message = "email field is empty")
    @Pattern(regexp = ".*@acme\\.com$", message = "invalid email")
    private String employee;
    @NotBlank(message = "period field is empty")
    @JsonFormat(pattern = "MM-yyyy")
    @Pattern(regexp = "^(0[1-9]|1[0-2])-(19|20)\\d{2}$", message = "date must be of valid MM-yyyy format")
    private String period;

    public Date getDateFromString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-yyyy");
        try {
            return dateFormat.parse(this.period);
        } catch (ParseException e) {
            throw new ValidationException("invalid date format");
        }
    }
}