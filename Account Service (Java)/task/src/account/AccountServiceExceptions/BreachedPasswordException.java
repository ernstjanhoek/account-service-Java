package account.AccountServiceExceptions;

public class BreachedPasswordException extends RuntimeException {
    public BreachedPasswordException() {
        super("The password is in the hacker's database!");
    }
}
