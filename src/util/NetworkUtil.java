package util;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class NetworkUtil {
    public static InetAddress getLocalHostAddress() {
        DatagramSocket socket = null;
        InetAddress localHostAddress = null;

        try {
            //InetAddress.getLocalHost(); is not reliable, it sometimes returns just the local loopback address
            socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            localHostAddress = socket.getLocalAddress();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

        if (localHostAddress == null || localHostAddress.isLoopbackAddress()) {
            //not good :(
            try {
                //this is better than nothing
                localHostAddress = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return localHostAddress;
    }

    public static String addressToString(InetSocketAddress... addresses) {
        String s = "";
        for(InetSocketAddress address : addresses) {
            s = s + address.getAddress().getHostAddress() + ":" + address.getPort() + " ";
        }
        return s;
    }
}
