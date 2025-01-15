package Model.Database;

public class DataManagerException extends Exception {
    public DataManagerException() {

    }

    public DataManagerException(String message) {
        super(message);
    }

    public DataManagerException(Throwable cause) {
        super(cause);
    }

    public DataManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
