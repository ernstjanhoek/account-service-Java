package account;

import account.AccountServiceExceptions.AdminDeletionException;
import account.AccountServiceExceptions.BreachedPasswordException;
import account.AccountServiceExceptions.PasswordEqualsException;
import account.AccountServiceExceptions.UserExistsException;
import account.DTO.ErrorResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(value = {
            BreachedPasswordException.class,
            DataIntegrityViolationException.class,
            PasswordEqualsException.class,
            UserExistsException.class,
            EntityExistsException.class,
            HttpMessageNotReadableException.class,
            ParseException.class,
            ConstraintViolationException.class,
            AdminDeletionException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> badRequestHandler(Exception e,
                                                           HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDate.now(),
                400,
                "Bad Request",
                e.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {
            EntityNotFoundException.class,
    })
    public ResponseEntity<ErrorResponse> notFoundHandler(Exception e,
                                                         HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDate.now(),
                404,
                "Not Found",
                e.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArgument(MethodArgumentNotValidException e,
                                                               HttpServletRequest request) {
        List<String> messages = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        String message = String.join(", ", messages);
        ErrorResponse body = new ErrorResponse(
                LocalDate.now(),
                400,
                "Bad Request",
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}