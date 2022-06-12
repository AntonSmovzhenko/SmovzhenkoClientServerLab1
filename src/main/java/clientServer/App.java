package clientServer;

import java.security.NoSuchAlgorithmException;

public class App
{
    public static void main(String[] args) {

        message message = new message(1,1,new MessageObject("message"));

        byte srcId = 34;

        PacketSerializer packetSerializer = new PacketSerializer(message, srcId);

        Packet p = new Packet(packetSerializer.getPacket());

    }
}
