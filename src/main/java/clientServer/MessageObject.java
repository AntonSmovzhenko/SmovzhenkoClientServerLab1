package clientServer;

public class MessageObject {
    private String message;

    public MessageObject() {
        
    }
    public MessageObject(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
