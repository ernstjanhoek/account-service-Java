package account.AccountServiceExceptions;

public class UserExistsException extends RuntimeException {
    public UserExistsException() {
        super("User exist!");
    }
}
