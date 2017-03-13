import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager class for connecting to other peers
 */
public class PeerConnection {
    private final Listener listener;
    private final InetSocketAddress rendezVousServer;
    private final int ownId;
    private final Map<Integer, Connector> runningConnectors;
    private int timeout = 3000;

    /**
     * This class allows to connect to one or more other peers.
     *
     * @param rendezVousServer the rendezvous server. this is required for a peer to peer connection across NATs
     * @param ownId            the id of this peer
     * @param listener         called on events like timeout or success
     */
    public PeerConnection(InetSocketAddress rendezVousServer, int ownId, Listener listener) {
        if (listener == null) throw new NullPointerException("Listener can't be null");

        this.ownId = ownId;
        this.rendezVousServer = rendezVousServer;
        this.listener = listener;

        runningConnectors = new HashMap<>();
    }

    /**
     * Set a custom socket read timeout for the connections. Default is 3000ms.
     *
     * @param millis timeout in millis.
     */
    public void setTimeout(int millis) {
        this.timeout = millis;
    }

    /**
     * Start to connect to another peer
     *
     * @param id the id of the other peer
     * @throws SocketException if something goes wrong
     */
    public void connect(int id) throws SocketException {
        if (runningConnectors.containsKey(id))
            throw new IllegalArgumentException("already connecting to " + id);

        Connector c = new Connector(this, ownId, id, timeout);
        runningConnectors.put(id, c);
        c.start(rendezVousServer);
    }

    /**
     * Cancels everything. You guessed it.
     */
    public void cancel() {
        for (Integer id : runningConnectors.keySet()) {
            cancel(id);
        }
    }

    /**
     * Cancels the attempt to connect to this id
     *
     * @param id of another peer
     */
    public void cancel(int id) {
        if (runningConnectors.containsKey(id)) {
            runningConnectors.remove(id).cancel();
        }
    }

    void onTimout(int otherId, int state) {
        System.out.println("Timout while connecting to " + otherId + " state: " + state);
        runningConnectors.remove(otherId);
        listener.onTimout(otherId);
    }

    void success(final int otherId, final DatagramSocket socket, final InetSocketAddress otherAddress) {
        //delay one second to ensure the other peer receives "connction confirmed"
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        runningConnectors.remove(otherId);
                        listener.onSuccess(otherId, socket, otherAddress);
                    }
                }, 1000
        );
    }

    public interface Listener {
        void onTimout(int id);

        void onSuccess(int id, DatagramSocket socket, InetSocketAddress targetAddress);
    }
}
