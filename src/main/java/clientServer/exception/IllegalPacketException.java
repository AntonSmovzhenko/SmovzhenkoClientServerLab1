package clientServer.exception;

public class IllegalPacketException extends RuntimeException {
    public IllegalPacketException(String message) {
        super(message);
    }
}
