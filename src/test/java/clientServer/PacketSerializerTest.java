package clientServer;

import clientServer.cipher.CipherString;
import clientServer.cipher.Crc16Checker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PacketSerializerTest {
    public static final int MESSAGE_POSITION = 16;

    @Test
    public void serializesMessageIds(){
        int expectedCommand = 60;
        int expectedUserId = 50;
        message message = new message(expectedCommand, expectedUserId, new MessageObject());

        PacketSerializer packetSerializer = new PacketSerializer(message, (byte) 0);
        ByteBuffer byteBuffer = ByteBuffer.wrap(packetSerializer.getPacket());
        byteBuffer.position(MESSAGE_POSITION);

        assertEquals(expectedCommand, byteBuffer.getInt());
        assertEquals(expectedUserId, byteBuffer.getInt());
    }
    @Test
    public void CiphersAndAddsMessageObjectToMessage(){
        MessageObject expectedMessageObject = new MessageObject("expected");
        byte[] encryptedMessage = cipherMessageObject(expectedMessageObject);
        message message = new message(0,0, expectedMessageObject);

        PacketSerializer packetSerializer = new PacketSerializer(message, (byte) 0);
        ByteBuffer byteBuffer = ByteBuffer.wrap(packetSerializer.getPacket());
        byteBuffer.position(MESSAGE_POSITION + Integer.BYTES * 2);

        byte[] actualMessage = new byte[encryptedMessage.length];
        byteBuffer.get(actualMessage, 0, encryptedMessage.length);
        assertArrayEquals(encryptedMessage, actualMessage);
    }

    @Test
    public void serializesIdsAndMagicByte(){
        byte expectedSrcId = 4;
        long expectedPacketId = 40;
        message message = new message(0,0,new MessageObject());

        PacketSerializer packetSerializer = new PacketSerializer(message, expectedSrcId, expectedPacketId);
        ByteBuffer byteBuffer = ByteBuffer.wrap(packetSerializer.getPacket());

        assertEquals(Packet.bMagic, byteBuffer.get());
        assertEquals(expectedSrcId, byteBuffer.get());
        assertEquals(expectedPacketId, byteBuffer.getLong());
    }
    @Test
    public void addsMessageLengthToPacket(){
        MessageObject messageObject = new MessageObject("expected");
        message message = new message(0,0, messageObject);
        byte[] encryptedMessage = cipherMessageObject(messageObject);
        int expectedMessageLength = encryptedMessage.length + Integer.BYTES * 2;

        PacketSerializer packetSerializer = new PacketSerializer(message, (byte) 0);
        ByteBuffer byteBuffer = ByteBuffer.wrap(packetSerializer.getPacket());
        byteBuffer.position(MESSAGE_POSITION - Integer.BYTES - Short.BYTES);

        int actualLength = byteBuffer.getInt();
        assertEquals(expectedMessageLength, actualLength);
    }

    private byte[] cipherMessageObject(MessageObject messageObject){
        try {
            CipherString cipherString = new CipherString();
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonMessage = objectMapper.writeValueAsString(messageObject);
            return cipherString.encrypt(jsonMessage);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
