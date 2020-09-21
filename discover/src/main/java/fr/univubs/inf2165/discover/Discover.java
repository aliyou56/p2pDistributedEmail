package fr.univubs.inf2165.discover;

import fr.univubs.inf2165.gossiper.Gossiper;
import fr.univubs.inf2165.gossiper.GossipingSession;
import fr.univubs.inf2165.gossiper.format.Address;
import fr.univubs.inf2165.gossiper.format.MessageType;
import fr.univubs.inf2165.gossiper.format.UserInfo;
import fr.univubs.inf2165.gossiper.format.Util;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;

/**
 *
 * @author aliyou sylla
 * @version 1.0.0
 */
public class Discover extends Thread {

    private static final int BUFFER_SIZE = 1 + 4 + 2 + 1 + 256;

    private Address groupAddress;
    private Gossiper gossiper;
    private HashMap<String, LocalDate> neighbors;

    private int delay = 5; // seconds

    private boolean running = false;

    /**
     * Constructor with the group address, group port and a gossiper server.
     *
     * @param groupAddress The group address
     * @param groupPort The group port
     * @param gossiper The gossiper server
     * @param delay The delay (seconds) before announcing it's presence
     * @throws IOException
     */
    public Discover(String groupAddress, short groupPort, Gossiper gossiper, int delay) throws IOException {
        this(groupAddress, groupPort, gossiper);
        this.delay = delay;
    }

    /**
     * Constructor with the group address, group port and a gossiper server.
     *
     * @param groupAddress The group address
     * @param groupPort The group port
     * @param gossiper The gossiper server
     * @throws IOException
     */
    public Discover(String groupAddress, short groupPort, Gossiper gossiper) throws IOException {
        Util.checkNotNull("Discover -> groupAddress", groupAddress);
        Util.checkNotNull("Discover -> gossiper", gossiper);
        this.groupAddress = new Address(groupAddress, groupPort);
        this.gossiper = gossiper;
        this.neighbors = new HashMap<>();
        System.out.println("\n[Discover]: groupAddress -> " + groupAddress);
        System.out.println("[Discover]: groupPort    -> " + groupPort);
    }

    @Override
    public void run() {
        this.running = true;

        MulticastPublisher publisher = new MulticastPublisher();
        publisher.start();

        MulticastReceiver receiver = new MulticastReceiver();
        receiver.run();
    }

    public synchronized void stopServer() {
        this.running = false;
    }

    /**
     *
     */
    class MulticastReceiver extends Thread {

        @Override
        public void run() {
            byte[] buffer = new byte[BUFFER_SIZE];
            try (MulticastSocket multicastSocket = new MulticastSocket(groupAddress.getPort())){
                //Joint the Multicast group.
                multicastSocket.joinGroup(groupAddress.getIp());
                while (running) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    System.out.println("[Discover]: waiting for data ... ");
                    multicastSocket.receive(packet);
                    //System.out.println("packet : " +packet.toString());
                    //System.out.println("length : " +packet.getLength());
                    //System.out.println("\t from : " +packet.getAddress() + " : " + packet.getPort());
                    ByteBuffer recBuf = ByteBuffer.wrap(packet.getData());
                    //ByteBuffer recBuf = ByteBuffer.wrap(buffer, 0, buffer.length);
                    if(recBuf != null) {
                        byte code = recBuf.get();
                        recBuf.position(0);
                        //System.out.println(" code = " + code);
                        MessageType messageType = MessageType.getMessageType(code);
                        if (messageType != null && messageType == MessageType.BEACON) {
                            BeaconMessageFormat message = new BeaconMessageFormat(recBuf);
                            System.out.println("[Discover]: Receive -> " + message);
                            if (!neighbors.containsKey(message.getUserInfo().getUsername())) {
                                System.out.println("[Discover]: never see the user -> " + message.getUserInfo().getUsername());
                                neighbors.put(message.getUserInfo().getUsername(), LocalDate.now());
                                try (GossipingSession session = new GossipingSession(gossiper, message.getAddress())) {
                                    session.run();
                                }
                            } else {
                                LocalDate date = neighbors.get(message.getUserInfo().getUsername());
                                LocalDate now = LocalDate.now();
                                long duration = Duration.between(date, now).getSeconds();
                                if (duration > delay) {
                                    System.out.println("[Discover]: it's been a while -> " + message.getUserInfo().getUsername());
                                    neighbors.put(message.getUserInfo().getUsername(), now);
                                    try (GossipingSession session = new GossipingSession(gossiper, message.getAddress())) {
                                        session.run();
                                    }
                                }
                            }
                        } else {
                            System.out.println("[Discover]: unknown message type received.");
                        }
                    }
                }
            } catch (IOException ioe) {
                System.err.println("[Discover]: error while receiving multicast message\n\t cause -> " + ioe.getMessage());
                //ioe.printStackTrace(System.out);
            }
        }
    }

    /**
     *
     */
    class MulticastPublisher extends Thread {

        @Override
        public void run() {
            try(MulticastSocket multicastSocket = new MulticastSocket()) {
                byte[] buffer;
                //System.out.println("gossiper ip : " + gossiper.getIP());
                Address address = new Address(gossiper.getIP(), gossiper.getUdpPort());
                UserInfo userInfo = new UserInfo(gossiper.getUsername());
                while (running) {
                    BeaconMessageFormat message = new BeaconMessageFormat(address, userInfo);
                    System.out.println("[Discover]: Publish -> " + message);
                    buffer = message.getPacket().array();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, groupAddress.getIp(), groupAddress.getPort());
                    multicastSocket.send(packet);
                    Thread.sleep(delay * 1000); // sleep delay seconds
                }
            } catch (InterruptedException | IOException e) {
                System.err.println("[Discover]: error while publishing multicast message\n\t cause -> " + e.getMessage());
                //e.printStackTrace(System.out);
            }
        }
    }
}
