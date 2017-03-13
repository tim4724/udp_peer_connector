import java.net.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.print("\nEnter your id: ");
        int ownId = scanner.nextInt();
        System.out.print("\nEnter other id: ");
        int otherId = scanner.nextInt();

        InetAddress inetAddress = InetAddress.getByName("tv_test.dd-dns.de");
        InetSocketAddress rendezVousServer = new InetSocketAddress(inetAddress, 3845);

        PeerConnection peerConnection = new PeerConnection(rendezVousServer, ownId, new PeerConnection.Listener() {
            @Override
            public void onTimout(int id) {
                System.out.println("!!!!timeout");
            }

            @Override
            public void onSuccess(int id, DatagramSocket socket, InetSocketAddress targetAddress) {
                System.out.println("!!!!success");
            }
        });

        peerConnection.connect(otherId);
    }
}
