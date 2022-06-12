package clientServer;

import clientServer.cipher.CipherString;
import clientServer.cipher.Crc16Checker;
import clientServer.exception.IllegalPacketException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class PacketSerializer {
    private static long packetId = 0;
    private byte[] packet;
    private static final int PACKET_START_LENGTH = 16;

    public PacketSerializer(message message, byte srcId, long packetId) {

        byte[] encrypted;

        try {

            CipherString cipherString = new CipherString();
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonMessage = objectMapper.writeValueAsString(message.getMessageObject());
            encrypted = cipherString.encrypt(jsonMessage);

        } catch(Exception e) {
            throw new IllegalPacketException(e.getMessage());
        }

        //packet start serialization
        int messageLength = Integer.BYTES * 2 +  encrypted.length;
        int packetLength = PACKET_START_LENGTH + Short.BYTES + messageLength;

        ByteBuffer byteBuffer = ByteBuffer.allocate(packetLength);
        byteBuffer.put(Packet.bMagic);
        byteBuffer.put(srcId);
        byteBuffer.putLong(packetId);
        byteBuffer.putInt(messageLength);

        Crc16Checker crc16Checker = new Crc16Checker();

        byte[] packetStart = Arrays.copyOf(byteBuffer.array(), byteBuffer.position());
        short packetCrc16 = crc16Checker.createCrc16(packetStart);
        byteBuffer.putShort(packetCrc16);

        ByteBuffer messageByteBuffer = ByteBuffer.allocate(messageLength);

        messageByteBuffer.putInt(message.getCommandType());
        messageByteBuffer.putInt(message.getUserId());
        messageByteBuffer.put(encrypted);

        byteBuffer.put(messageByteBuffer.array());

        short messageCrc16 = crc16Checker.createCrc16(messageByteBuffer.array());
        byteBuffer.putShort(messageCrc16);

        packet = byteBuffer.array();
    }

    public PacketSerializer(message message, byte srcId) {
        this(message, srcId, packetId++);
    }

    public static long getPacketId() {
        return packetId;
    }

    public byte[] getPacket() {
        return packet;
    }
}
