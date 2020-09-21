package fr.univubs.inf2165.filesender;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

/**
 * This program allows user to send a file over a network. It uses
 * the hostname and the port number to establish a connection
 * with the receiver
 *
 * @author Aliyou Sylla
 * @version 08/10/2019
 */

public class FileSender implements Closeable {

    private String host;
    private int port;

    /**
     * SocketChannel
     */
    private SocketChannel socketChannel = null;
    private boolean closed = true;

    /**
     * Constructor with the hostname and the port number
     *
     * @param host The hostname. Must not be null.
     * @param port The port number
     * @throws IOException
     */
    public FileSender(String host, int port) throws IOException {
        if (host == null) {
            throw new NullPointerException("host == null");
        }
        this.host = host;
        this.port = port;
        open();
    }

    /**
     * Establish a socket connection with the host at the given port number.
     *
     * @throws IOException if the connection can't be established.
     */
    public void open() throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(this.host, this.port);
        socketChannel = SocketChannel.open(socketAddress);
        this.closed = false;
        System.out.println("[FileSender]: Session opened with -> " + this.host + ":" + this.port);
    }

    /**
     * Sends a file located at the given path to the connected socket channel.
     *
     * @param path The path of the file to be sent.
     * @return The number of bytes, possibly zero, that were actually sent.
     * @throws IOException if some input/output error occurs.
     */
    public long sendFile(Path path) throws IOException {
        long bytesSent = 0;
        if (path.toFile().exists()) {
            if (!path.toFile().isDirectory()) {
                try (FileChannel inChannel = FileChannel.open(path)) {
                    bytesSent = inChannel.transferTo(0, inChannel.size(), this.socketChannel);
                    System.out.println("[FileSender]: File successfully sent -> " + bytesSent + " bytes sent !");
                }
            } else {
                System.out.println("[FileSender]: \'" + path + "\' is a directory.");
            }
        } else {
            System.out.println("[FileSender]: file not found -> "+ path);
        }
        return bytesSent;
    }

    @Override
    public synchronized void close() {
        if (!this.closed) {
            this.closed = true;
            try {
                this.socketChannel.close();
                System.out.println("[FileSender]: Session closed with -> " + this.host + ":" + this.port);
            } catch (IOException ioe) {
                System.err.println("[FileSender]: Error while closing the session -> " + ioe.getMessage());
            }
        }
    }

}