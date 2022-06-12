package clientServer;

import clientServer.cipher.Crc16Checker;
import clientServer.exception.IllegalPacketException;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Packet {
    protected static final byte bMagic = 0x13;
    private static final int PACKAGE_BYTES_LENGTH = 14;
    private byte srcId;
    private long packetId;
    private clientServer.message message;
    private int messageLength;
    private short packageCrc16;
    private short messageCrc16;
    private Crc16Checker crc16Checker = new Crc16Checker();

    public Packet(byte[] bytePacket) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytePacket);
        byte magic = byteBuffer.get();
        if(magic != bMagic) {
            throw new IllegalPacketException("Wrong first byte");
        }

        this.srcId = byteBuffer.get();
        this.packetId = byteBuffer.getLong();
        this.messageLength = byteBuffer.getInt();
        this.packageCrc16 = byteBuffer.getShort();

        byte[] packageBytes = Arrays.copyOf(bytePacket, PACKAGE_BYTES_LENGTH);

        if(!crc16Checker.checkCrc16(packageBytes, this.packageCrc16)) {
            throw new IllegalPacketException("Packet is damaged");
        }

        this.message = new message(byteBuffer, this.messageLength);
        this.messageCrc16 = byteBuffer.getShort();
        byte[] messageBytes = Arrays.copyOfRange(bytePacket, PACKAGE_BYTES_LENGTH + Short.BYTES, PACKAGE_BYTES_LENGTH + Short.BYTES + this.messageLength);
        if(!crc16Checker.checkCrc16(messageBytes, this.messageCrc16)) {
            throw new IllegalPacketException("Packet is damaged");
        }
    }
    public long getPacketId() {
        return packetId;
    }
    public byte getSrcId() {
        return srcId;
    }
    public clientServer.message getMessage() {
        return message;
    }

}
