package clientServer;

import clientServer.cipher.CipherString;
import clientServer.cipher.Crc16Checker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PacketSerializerTest {
    public static final int POSITION_OF_MESSAGE = 16;
    @Test
    public void SerializesRightIdsAndMagicByte(){
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
    public void SerializesRightMessageIds(){
        int expectedCommand = 60;
        int expectedUserId = 50;
        message message = new message(expectedCommand, expectedUserId, new MessageObject());

        PacketSerializer packetSerializer = new PacketSerializer(message, (byte) 0);
        ByteBuffer byteBuffer = ByteBuffer.wrap(packetSerializer.getPacket());
        byteBuffer.position(POSITION_OF_MESSAGE);

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
        byteBuffer.position(POSITION_OF_MESSAGE + Integer.BYTES * 2);

        byte[] actualMessage = new byte[encryptedMessage.length];
        byteBuffer.get(actualMessage, 0, encryptedMessage.length);
        assertArrayEquals(encryptedMessage, actualMessage);
    }
    @Test
    public void AddsRightMessageLengthToPacket(){
        MessageObject messageObject = new MessageObject("expected");
        message message = new message(0,0, messageObject);
        byte[] encryptedMessage = cipherMessageObject(messageObject);
        int expectedMessageLength = encryptedMessage.length + Integer.BYTES * 2;

        PacketSerializer packetSerializer = new PacketSerializer(message, (byte) 0);
        ByteBuffer byteBuffer = ByteBuffer.wrap(packetSerializer.getPacket());
        byteBuffer.position(POSITION_OF_MESSAGE - Integer.BYTES - Short.BYTES);

        int actualLength = byteBuffer.getInt();
        assertEquals(expectedMessageLength, actualLength);
    }
    @Test
    public void CalculatesAndAddsCrc16ValuesToPacket() {
        Crc16Checker crc16Checker = new Crc16Checker();
        MessageObject messageObject = new MessageObject("expected");
        message message = new message(0,0, messageObject);
        byte[] encryptedMessage = cipherMessageObject(messageObject);
        ByteBuffer messageByteBuffer = ByteBuffer.allocate(encryptedMessage.length + Integer.BYTES * 2);
        messageByteBuffer.putInt(message.getCommandType());
        messageByteBuffer.putInt(message.getUserId());
        messageByteBuffer.put(encryptedMessage);
        short expectedMessageCrc16 = crc16Checker.createCrc16(messageByteBuffer.array());

        ByteBuffer packetBuffer = ByteBuffer.allocate(POSITION_OF_MESSAGE - Short.BYTES);
        packetBuffer.put(Packet.bMagic);
        packetBuffer.put((byte) 0);
        packetBuffer.putLong(0l);
        packetBuffer.putInt(encryptedMessage.length + Integer.BYTES * 2);
        short expectedPacketCrc16 = crc16Checker.createCrc16(packetBuffer.array());

        PacketSerializer packetSerializer = new PacketSerializer(message, (byte) 0, 0l);
        ByteBuffer byteBuffer = ByteBuffer.wrap(packetSerializer.getPacket());

        short actualPacketCrc16 = byteBuffer.getShort(POSITION_OF_MESSAGE - Short.BYTES);
        short actualMessageCrc16 = byteBuffer.getShort(POSITION_OF_MESSAGE + Integer.BYTES * 2 + encryptedMessage.length);
        assertEquals(expectedMessageCrc16, actualMessageCrc16);
        assertEquals(expectedPacketCrc16, actualPacketCrc16);
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
