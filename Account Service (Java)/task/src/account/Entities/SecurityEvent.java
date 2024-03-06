package account.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityEvent {
    @Id
    @GeneratedValue
    private Long id;
    @NotNull
    private LocalDate date;
    @NotNull
    private String action;
    @Column(columnDefinition = "VARCHAR(255) DEFAULT 'Anonymous'")
    private String subject = "Anonymous";
    @NotNull
    private String object;
    @NotNull
    private String path;
    public SecurityEvent(
            LocalDate date,
            String action,
            String subject,
            String object,
            String path
    ) {
        this.date = date;
        this.action  = action;
        this.subject = subject;
        this.object = object;
        this.path = path;
    }
    public SecurityEvent(
            LocalDate date,
            String action,
            String object,
            String path
    ) {
        this.date = date;
        this.action  = action;
        this.object = object;
        this.path = path;
    }
}