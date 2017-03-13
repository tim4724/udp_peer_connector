import util.ByteStuff;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static util.ByteStuff.*;

/**
 * Put data in a byte array and read data from a byte array
 */
class Payload {
    static final int length = 28;

    private static final int senderIdIndex = 0;         //4 Bytes
    private static final int receiverIdIndex = 4;       //4 Bytes
    private static final int stateIndex = 8;            //4 Bytes
    private static final int localAddressIndex = 12;    //4 Bytes
    private static final int localPortIndex = 16;       //4 Bytes
    private static final int publicAddressIndex = 20;   //4 Bytes
    private static final int publicPortIndex = 24;      //4 Bytes

    static byte[] build(int senderId, int receiverId, int state, InetAddress localAddress, int port) {
        byte[] buffer = new byte[length];
        putInt(senderId, buffer, senderIdIndex);
        putInt(receiverId, buffer, receiverIdIndex);
        putInt(state, buffer, stateIndex);
        putBytes(localAddress.getAddress(), buffer, localAddressIndex);
        putInt(port, buffer, localPortIndex);
        return buffer;
    }

    static InetSocketAddress[] getPeerAddresses(byte[] data) throws UnknownHostException {
        return new InetSocketAddress[]{getLocalAddress(data), getPublicAddress(data)};
    }

    private static InetSocketAddress getLocalAddress(byte[] data) throws UnknownHostException {
        byte[] localIp = subBytes(data, localAddressIndex, localAddressIndex + 4);
        int localPort = readInt(data, localPortIndex);
        InetAddress localInetAddress = InetAddress.getByAddress(localIp);
        return new InetSocketAddress(localInetAddress, localPort);
    }

    private static InetSocketAddress getPublicAddress(byte[] data) throws UnknownHostException {
        byte[] publicIp = subBytes(data, publicAddressIndex, publicAddressIndex + 4);
        int publicPort = readInt(data, publicPortIndex);
        InetAddress publicInetAddress = InetAddress.getByAddress(publicIp);
        return new InetSocketAddress(publicInetAddress, publicPort);
    }

    static int getSenderId(byte[] data) {
        return ByteStuff.readInt(data, senderIdIndex);
    }

    static int getReceiverId(byte[] data) {
        return ByteStuff.readInt(data, receiverIdIndex);
    }

    static int getState(byte[] data) {
        return ByteStuff.readInt(data, stateIndex);
    }

    /**
     * Possible States during the attempt to connect
     */
    class ConnState {
        static final int connectToServer = 1, requestPeerAddresses = 2, connectToPeer = 3, confirmingConnection = 4;
    }
}
