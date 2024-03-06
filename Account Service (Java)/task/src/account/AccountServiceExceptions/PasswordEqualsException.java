package account.AccountServiceExceptions;

public class PasswordEqualsException extends RuntimeException {
    public PasswordEqualsException() {
        super("The passwords must be different!");
    }
}
