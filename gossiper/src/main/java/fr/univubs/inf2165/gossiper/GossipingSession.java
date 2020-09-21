package fr.univubs.inf2165.gossiper;

import fr.univubs.inf2165.gossiper.format.Address;
import fr.univubs.inf2165.gossiper.format.MessageType;
import fr.univubs.inf2165.gossiper.format.Util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class represents a gossiping session with a peer. In order to be able
 * to start, a session needs a gossiper server and the peer address.
 *
 * @author Aliyou Sylla
 * @version 1.0.1
 */
public class GossipingSession extends Thread implements Closeable {

    private Gossiper gossiper;
    private Address peerAddress;

    private DatagramChannel channel = null; // the channel
    private boolean close = true; // true if the session is closed.

    /**
     * Constructs a new GossipingSession object.
     *
     * @param gossiper The gossiper server
     * @param peerAddress The address of the peer.
     * @throws IOException if the session can not be opened.
     */
    public GossipingSession(Gossiper gossiper, Address peerAddress) throws IOException {
        Util.checkNotNull("GossipingSession -> gossiper", gossiper);
        Util.checkNotNull("GossipingSession -> peerAddress", peerAddress);
        this.gossiper = gossiper;
        this.peerAddress = peerAddress;
        this.open();
    }

    /**
     * Open the gossiping session
     * @throws IOException if the session can not be opened.
     */
    public void open() throws IOException {
        this.channel = DatagramChannel.open();
        this.close = false;
        System.out.println("[Gossiper]: Session started with -> " + this.peerAddress);
    }

    @Override
    public synchronized void close() {
        if( ! this.close) {
            this.close = true;
            try {
                channel.close();
                System.out.println("[Gossiper]: Session closed  with -> " + this.peerAddress + "\n");
            } catch(IOException ioe) {
                System.err.println("Error while closing the gossiping session : " + ioe.getMessage());
            }
        }
    }

    @Override
    public void run() {
        try {
            // offering files to the peer
            offeringFiles(this.gossiper.getUserDirectory());
        } catch(IOException ioe) {
            System.err.println("I/O error occurs: " + ioe.getMessage());
        }
    }

    /**
     *
     * @param dir
     * @throws IOException
     */
    private void offeringFiles(Path dir) throws IOException {
        //Set<Path> fileList = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            stream.forEach( (path) -> {
                try {
                    if (Files.isDirectory(path)) {
                        if ( ! path.equals(this.gossiper.getRecvDirectory())) {
                            offeringFiles(path);
                        }
                    } else { // path is a file
                        // if the file is not in the send directory make a copy of the file in the send directory
                        if(path.getParent().equals(this.gossiper.getSendDirectory())) {
                            this.send(MessageType.OFFER, path.getFileName().toString());
                        } else {
                            Path filepath = this.gossiper.getSendDirectory().resolve(path.getFileName());
                            if( ! Files.exists(filepath)) {
                                Files.copy(path, filepath);
                            }
                        }
                    }
                } catch(IOException ioe) {
                    ioe.printStackTrace(System.out);
                    System.err.println("GossipingSession -> offeringFiles -> I/O error occurs: " + ioe.getMessage());
                }
            });
        }
    }

    /**
     * Send a command to the peer.
     *
     * @param messageType The type of the message (OFFER | REQUEST | DELETE)
     * @param filename The file name
     * @throws IOException if an IO error occurs
     */
    public void send(MessageType messageType, String filename) throws IOException {
        switch (messageType) {
            case OFFER:{
                this.gossiper.sendOffer(channel, filename, this.peerAddress.getInetSocketAddress());
                break;
            }
            case REQUEST: {
                this.gossiper.sendRequest(channel, filename, this.peerAddress.getInetSocketAddress());
                break;
            }
            case DELETE: {
                this.gossiper.sendDelete(channel, filename, this.peerAddress.getInetSocketAddress());
                break;
            }
        }
    }

}
