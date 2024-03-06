package account.DTO;

import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class PaymentResponse {
    private String name;
    private String lastname;
    private String period;
    private String salary;
    public void setSalary(Long salary) {
        Long cents = salary % 100;
        Long cardinals = (salary - cents) / 100;
        this.salary = cardinals +" dollar(s) " + cents + " cent(s)";
    }
    public void setPeriod(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("MMMM-yyyy");
        this.period = dateFormat.format(date);
    }
}