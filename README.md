# udp_peer_connector

Establish a direct UDP peer-to-peer connection between to clients using an technique called udp hole punching. 

This may not work on all networks, it usually doesn't work on mobile internet.
Special thanks to the guy who wrote this paper: http://www.brynosaurus.com/pub/net/p2pnat/

1. Set up the rendezvous Server https://github.com/timbirdy/udp_peer_rendezvous_server

2. Build a jar file from this Project and add it as a library to your Project. You can also just copy the source files to your project.

3. Implement the following code in your client.
```
    import de.tim.udp_connector.PeerConnection;
    
    public class YourClass implements PeerConnection.Listener {
    
        public void doMagic() throws SocketException, UnknownHostException {
            InetSocketAddress serverAddress = new InetSocketAddress(InetAddress.getByName("address.server.com"), 3845));
            int myId = 1557; int otherId = 16;      //give each peer an id
            
            PeerConnection connection = new PeerConnection(serverAddress, myId, this);
            connection.connect(otherId);// you can connect to another peer by knowing its id
            //connection.connect(17);   //you can connect to multiple other peers at the same time
        }

        @Override
        public void onTimout(int id) {
            //timeout occurred while trying to connect to the peer with the given id
        }

        @Override
        public void onSuccess(int id, DatagramSocket socket, InetSocketAddress targetAddress) {
            //successfully connected to the peer with id "id" 
            //use the provided socket to send packets to this peer with the provided address
        }
    }
```
