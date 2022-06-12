package clientServer;

import clientServer.cipher.CipherString;
import clientServer.exception.IllegalPacketException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;

public class message {

    private int commandType = 0;

    private int userId = 0;

    private MessageObject messageObject;

    public message(ByteBuffer byteBuffer, int length) {

        this.commandType = byteBuffer.getInt();
        this.userId = byteBuffer.getInt();

        int lengthOfMessage = length - (Integer.BYTES * 2);

        if(lengthOfMessage < 0) {
            throw new IllegalPacketException("Wrong message length");
        }

        byte[] message = new byte[lengthOfMessage];
        byteBuffer.get(message, 0, lengthOfMessage);

        try {

            var cipherString = new CipherString();

            var string  = cipherString.decrypt(message);

            var objectMapper = new ObjectMapper();

            messageObject = objectMapper.readValue(string, MessageObject.class);

        } catch (Exception e) {
            throw new IllegalPacketException(e.getMessage());
        }
    }

    public message(int commandType, int userId, MessageObject messageObject) {

        this.commandType = commandType;
        this.userId = userId;
        this.messageObject = messageObject;

    }

    public int getCommandType() {
        return commandType;
    }
    public int getUserId() {
        return userId;
    }
    public MessageObject getMessageObject() {
        return messageObject;
    }
}
