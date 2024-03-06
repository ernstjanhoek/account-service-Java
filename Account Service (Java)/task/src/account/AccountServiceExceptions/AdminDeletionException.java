package account.AccountServiceExceptions;

public class AdminDeletionException extends RuntimeException {
    public AdminDeletionException(String message) {
        super(message);
    }
}