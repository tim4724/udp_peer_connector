import util.NetworkUtil;

import java.io.IOException;
import java.net.*;

/**
 * Connector tries to connect to another peer.
 */
class Connector {
    private final DatagramSocket socket;
    private Sender sendThread;
    private final Receiver receiveThread;
    private final PeerConnection peerConnection;
    private final int ownId, otherId;
    private int state;

    /**
     * Constructor for Connector. Each Connector Instance can connect to one other peer
     *
     * @param peerConnection notify about timeout or success
     * @param ownId          the own id of this peer
     * @param otherId        the id of the peer we want to connect to
     * @param timeout        timeout for each try
     * @throws SocketException if something goes wrong :)
     */
    Connector(PeerConnection peerConnection, int ownId, int otherId, int timeout) throws SocketException {
        this.peerConnection = peerConnection;
        this.ownId = ownId;
        this.otherId = otherId;
        socket = new DatagramSocket();
        socket.setSoTimeout(timeout);
        receiveThread = new Receiver();
    }

    /**
     * Send all necessary info to the rendezvous server
     *
     * @throws SocketException if something goes wrong :)
     */
    void start(InetSocketAddress rendezVousServer) throws SocketException {
        state = 0;
        newSendData(Payload.ConnState.connectToServer, rendezVousServer);
    }

    /**
     * New Data, whioh means a new connectingSate or new target addresses
     *
     * @param newState  the current connectingSate of this peer @see Payload.Connstate
     * @param addresses the new addresses
     * @throws SocketException if something goes wrong :)
     */
    private void newSendData(int newState, InetSocketAddress... addresses) throws SocketException {
        if (sendThread != null) sendThread.cancel();

        sendThread = new Sender(newState, addresses);
        receiveThread.setSender(sendThread);

        if (!receiveThread.isAlive()) receiveThread.start();
        sendThread.start();
    }

    /**
     * The sender sends given data to the given addresses.
     * If a timeout occurs the sender will be interrupted and the sender will send the data to the next available address.
     * If there are no mor available addresses the connect process has failed.
     */
    class Sender extends Thread {
        private InetSocketAddress[] targetAddresses;
        private final DatagramPacket packet;
        private boolean cancelled;

        Sender(int state, InetSocketAddress... targetAddresses) {
            byte[] payload = Payload.build(ownId, otherId, state, NetworkUtil.getLocalHostAddress(), socket.getLocalPort());
            packet = new DatagramPacket(payload, payload.length);
            this.targetAddresses = targetAddresses;
        }

        @Override
        public void run() {
            for (InetSocketAddress target : targetAddresses) {
                packet.setSocketAddress(target);
                System.out.println("Sender: Sending to " + NetworkUtil.addressToString((InetSocketAddress) packet.getSocketAddress()));
                while (!cancelled) {
                    try {
                        socket.send(packet);
                        sleep(200);
                    } catch (InterruptedException e) {
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (cancelled) break;
            }

            if (!cancelled) {
                peerConnection.onTimout(otherId, state);
                Connector.this.cancel();
            }
        }

        void cancel() {
            this.cancelled = true;
            this.interrupt();
        }
    }

    /**
     * The Receiver receives UDP Packets on the socket.
     *
     * @see #newSendData(int newState, InetSocketAddress... addresses) will be called if new data is received
     * <p>
     * if a timeout occurs, the sender will be interrupted
     */
    class Receiver extends Thread {
        private Sender sender;
        private boolean cancelled;

        void setSender(Sender sender) {
            this.sender = sender;
        }

        @Override
        public void run() {
            System.out.println("Receiver start");
            byte[] buffer = new byte[Payload.length];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (!cancelled) {
                try {
                    socket.receive(packet);

                    byte[] data = packet.getData();
                    int newState = Payload.getState(data);
                    if (newState > state) {
                        int senderId = Payload.getSenderId(data);
                        int receiverId = Payload.getReceiverId(data);

                        if (senderId == ownId && receiverId == otherId) {
                            //packet from server
                            if (newState == Payload.ConnState.connectToServer) {
                                System.out.println("Receiver: Registration confirmed");
                                newSendData(Payload.ConnState.requestPeerAddresses, (InetSocketAddress) packet.getSocketAddress());
                            } else if (newState == Payload.ConnState.requestPeerAddresses) {
                                System.out.println("Receiver: Received other peers addresses from server");
                                newSendData(Payload.ConnState.connectToPeer, Payload.getPeerAddresses(data));
                            }

                        } else if (senderId == otherId && receiverId == ownId) {
                            //packet from other peer
                            if (newState == Payload.ConnState.confirmingConnection) {
                                peerConnection.success(otherId, socket, (InetSocketAddress) packet.getSocketAddress());
                            }
                            newSendData(Payload.ConnState.confirmingConnection, (InetSocketAddress) packet.getSocketAddress());
                            System.out.println("Receiver: Received packet from other peer: " + NetworkUtil.addressToString((InetSocketAddress) packet.getSocketAddress()));
                        }
                        state = newState;
                    }
                } catch (SocketTimeoutException e) {
                    sender.interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void cancel() {
            this.cancelled = true;
            this.interrupt();
        }
    }

    /**
     * Guess what happens if this method is called.
     */
    void cancel() {
        sendThread.cancel();
        receiveThread.cancel();
    }
}
