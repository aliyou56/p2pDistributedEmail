package fr.univubs.inf2165.gossiper;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import fr.univubs.inf2165.filesender.FileSender;
import fr.univubs.inf2165.gossiper.format.*;

/**
 * This class represents a gossiper server.
 *
 * @author Aliyou Sylla
 * @version 1.0.1
 */
public class Gossiper extends Thread {

    /**
     * The size of the buffer
     */
    private static final int BUFFER_SIZE = 1 + 4 + 2 + 1 + 256 + 1 + 256;

    private String username;
    private Path baseDirectory;
    private short udpPort;
    private short tcpPort;
    private String ip;

    public static final boolean DEBUG = true;

    /**
     * Constructs a new Gossiper object whit the user name, the base directory, the UDP port
     * and the TCP port.
     *
     * @param username      The user name. Must not be null.
     * @param baseDirectory The base directory. Must not be null.
     * @param udpPort       The UDP port
     * @param tcpPort       The TCP port
     */
    public Gossiper(String username, Path baseDirectory, short udpPort, short tcpPort, String ip) throws IOException {
        this(username, baseDirectory, tcpPort, udpPort);
        Util.checkNotNull("Gossiper -> ip", ip);
        this.ip = ip;
    }

    /**
     * Constructs a new Gossiper object whit the user name, the base directory, the UDP port
     * and the TCP port.
     *
     * @param username      The user name. Must not be null.
     * @param baseDirectory The base directory. Must not be null.
     * @param udpPort       The UDP port
     * @param tcpPort       The TCP port
     */
    public Gossiper(String username, Path baseDirectory, short udpPort, short tcpPort) throws IOException {
        Util.checkNotNull("Gossiper -> username", username);
        Util.checkNotNull("Gossiper -> baseDirectory", baseDirectory);
        this.username = username;
        this.baseDirectory = baseDirectory;
        createDirectoryIfNotExists(this.getSendDirectory());
        createDirectoryIfNotExists(this.getRecvDirectory());
        this.udpPort = udpPort;
        this.tcpPort = tcpPort;
        this.ip = Inet4Address.getLocalHost().getHostName();
    }

    /**
     * Create a directory if it doesn't exist.
     *
     * @param dir Tjhe directory to create
     * @throws IOException
     */
    private void createDirectoryIfNotExists(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            Files.createDirectories(dir);
            System.out.println("[Gossiper]: I've just created the directory -> " + dir.toString());
        }
    }

    @Override
    public void run() {
        try (DatagramChannel channel = DatagramChannel.open()) {
            InetAddress inetAddress = Inet4Address.getByName(this.ip);
            InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, udpPort);
            channel.socket().bind(socketAddress);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            System.out.println("[Gossiper]: I'm running on " + socketAddress.getHostString() + ":" + udpPort);
            System.out.println("[Gossiper]: baseDirectory -> " + this.baseDirectory);
            System.out.println("[Gossiper]: username      -> " + this.username);
            while (true) {
                System.out.println("\n[Gossiper]: waiting for data ... ");
                channel.receive(buffer);
                buffer.flip();

                byte code = buffer.get();
                //System.out.println(" code = " + code);
                MessageType messageType = MessageType.getMessageType(code);
                buffer.position(0);
                if (messageType != null) {
                    switch (messageType) {

                        case OFFER: {
                            OfferMessageFormat message = new OfferMessageFormat(buffer);
                            System.out.println("[Gossiper]: " + message);
                            Path requestedFile = this.getRecvDirectory().resolve(message.getFileInfo().getFilename());
                            if (!Files.exists(requestedFile)) { // file is not in the user' receive directory
                                sendRequest(channel, message.getFileInfo().getFilename(), message.getAddress().getInetSocketAddress());
                            } else {
                                sendDelete(channel, message.getFileInfo().getFilename(), message.getAddress().getInetSocketAddress());
                            }
                            break;
                        }
                        case REQUEST: {
                            RequestMessageFormat message = new RequestMessageFormat(buffer);
                            System.out.println("[Gossiper]: " + message);
                            Path requestedFile = this.getSendDirectory().resolve(message.getFileInfo().getFilename());
                            if (Files.exists(requestedFile)) { // if file exists
                                String host = message.getAddress().getIp().getHostAddress();
                                try (FileSender fileSender = new FileSender(host, message.getAddress().getPort())) {
                                    fileSender.sendFile(requestedFile);
                                }
                            } else {
                                System.out.println("[Gossiper]: file not found -> " + message.getFileInfo().getFilename());
                            }
                            break;
                        }

                        case DELETE: {
                            DeleteMessageFormat message = new DeleteMessageFormat(buffer);
                            System.out.println("[Gossiper]: " + message);
                            Path requestedFile = this.getRecvDirectory().resolve(message.getFileInfo().getFilename());
                            System.out.print("[Gossiper]:");
                            System.out.print(Files.deleteIfExists(requestedFile) ? " file deleted" : " file not found");
                            System.out.println(" -> " + message.getFileInfo().getFilename());
                            break;
                        }
                    } // switch
                } else {
                    System.err.println("[Gossiper]: unknown message type received");
                }
                buffer.clear();
            }
        } catch (IOException ioe) {
            System.err.println("[Gossiper]: error while starting the server on the ip -> " + this.ip + "\n\t cause -> " + ioe.getMessage());
        }
    }

    /**
     * Sends a request packet to the given target.
     *
     * @param channel  The channel
     * @param filename The name of the requested file
     * @param target   The target to which the packet is sent.
     * @throws IOException
     */
    public void sendRequest(DatagramChannel channel, String filename, InetSocketAddress target) throws IOException {
        Address address = new Address(this.ip, this.tcpPort);
        UserInfo userInfo = new UserInfo(this.username);
        FileInfo fileInfo = new FileInfo(filename);
        RequestMessageFormat messageFormat = new RequestMessageFormat(address, userInfo, fileInfo);
        this.send(channel, messageFormat, target);
    }

    /**
     * Sends an offer packet to the given target.
     *
     * @param channel  The channel
     * @param filename The name of the offering file
     * @param target   The target to which the packet is sent.
     * @throws IOException
     */
    public void sendOffer(DatagramChannel channel, String filename, InetSocketAddress target) throws IOException {
        Address address = new Address(this.ip, this.udpPort);
        UserInfo userInfo = new UserInfo(this.username);
        FileInfo fileInfo = new FileInfo(filename);
        OfferMessageFormat messageFormat = new OfferMessageFormat(address, userInfo, fileInfo);
        this.send(channel, messageFormat, target);
    }

    /**
     * Sends a delete packet to the given target.
     *
     * @param channel  The channel
     * @param filename The name of the requested file to be deleted
     * @param target   The target to which the packet is sent.
     * @throws IOException
     */
    public void sendDelete(DatagramChannel channel, String filename, InetSocketAddress target) throws IOException {
        UserInfo userInfo = new UserInfo(this.username);
        FileInfo fileInfo = new FileInfo(filename);
        DeleteMessageFormat messageFormat = new DeleteMessageFormat(userInfo, fileInfo);
        this.send(channel, messageFormat, target);
    }

    /**
     * Sends the packet from the given message format to the given target.
     *
     * @param channel
     * @param messageFormat
     * @param target
     * @throws IOException
     */
    private void send(DatagramChannel channel, AbstractMessageFormat messageFormat, InetSocketAddress target) throws IOException {
        channel.send(messageFormat.getPacket(), target);
        if (DEBUG) System.out.println("[Gossiper]: packet sent -> " + messageFormat);
    }

    /**
     * Stops the server
     */
    public void stopServer() {
        this.interrupt();
        System.out.println("[Gossiper]: server stopped");
    }

    public Path getUserDirectory() {
        return this.baseDirectory.resolve(this.username);
    }

    public Path getSendDirectory() {
        return getUserDirectory().resolve("send");
    }

    public Path getRecvDirectory() {
        return getUserDirectory().resolve("recv");
    }

    /**
     * Return the IP address
     *
     * @return the IP address
     * @throws UnknownHostException
     */
    public String getIP() throws UnknownHostException {
        return this.ip;
        /*String ip;
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (IOException ioe) {
            ip = InetAddress.getLocalHost().getHostAddress();
        }
        return ip;*/
    }

    public String getUsername() {
        return username;
    }

    public Path getBaseDirectory() {
        return this.baseDirectory;
    }

    public short getUdpPort() {
        return udpPort;
    }

    public short getTcpPort() {
        return tcpPort;
    }

}
