package fr.univubs.inf2165.pop3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is an implementation of POP3 server.
 *
 * Inspired by : P. Launay' SMTP program code
 *
 * @author Aliyou Sylla
 * @version 14/10/2019
 */
public final class POP3Server extends Thread {

    public static final boolean DEBUG = true;

    private static final int BUFFER_SIZE = 512;

    private int port;
    /**
     * The base directory that contains mails.
     */
    private Path rootDirectory;

    private Map<SelectionKey, Session> sessions; // map of opened sessions

    /**
     * Constructor with the local port number and the base directory.
     *
     * @param port The local port number
     * @param baseDirectory The base directory. Must not be null.
     */
    public POP3Server(int port, Path baseDirectory) {
        if(baseDirectory == null) {
            throw new NullPointerException("baseDirectory == null");
        }
        this.port = port;
        this.rootDirectory = baseDirectory;
        if( ! this.rootDirectory.toFile().exists() || ! this.rootDirectory.toFile().isDirectory()) {
            this.rootDirectory.toFile().mkdirs();
            if(DEBUG) {
                System.out.println("S: I've just created the directory -> " + this.rootDirectory.toString());
            }
        }
        this.sessions = new HashMap<>();
    }

    @Override
    public void run() {
        try(
                ServerSocketChannel serverChannel = ServerSocketChannel.open();
                Selector selector = Selector.open();
        ) {
            // bind and register the server socket
            serverChannel.socket().bind(new InetSocketAddress(this.port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("S: POP3 server running on " + serverChannel.getLocalAddress());

            while(true) {

                try {
                    // blocking selection operation until at least on channel is selected
                    selector.select();

                    // process all the new events
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while(iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        if(key.isAcceptable()) {
                            accept(key, selector); // accep a client
                        } else if(key.isReadable()) {
                            read(key, selector); //read data
                        }
                    }
                } catch (IOException ioe) {
                    System.err.println(ioe.getMessage());
                }
            }
        } catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    /**
     * @param key
     * @param selector
     * @throws IOException
     */
    private void accept(SelectionKey key, Selector selector) throws IOException {
        SelectableChannel selectableChannel = key.channel();
        if(selectableChannel instanceof ServerSocketChannel) {
            // accept client
            SocketChannel socketChannel = ((ServerSocketChannel) selectableChannel).accept();
            if(socketChannel != null) {
                System.out.println("S: incoming connection from -> " + socketChannel.getRemoteAddress());
                // register the client
                socketChannel.configureBlocking(false);
                SelectionKey k = socketChannel.register(selector, SelectionKey.OP_READ);

                // create a client session associated to this key
                Session session = new Session(socketChannel, this.rootDirectory);
                session.open();
                sessions.put(k, session);
            }
        }
    }

    /**
     *
     * @param key
     * @param selector
     * @throws IOException
     */
    private void read(SelectionKey key, Selector selector) throws IOException {
        // get the socket and file channel
        SelectableChannel selectableChannel = key.channel();
        Session session = sessions.get(key);

        if(session != null && selectableChannel instanceof SocketChannel) {

            // init
            SocketChannel socketChannel = (SocketChannel) selectableChannel;
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            // read data from the socket
            int bytesRead = socketChannel.read(buffer);
            if(bytesRead > 0) {
                //session.handleRequest(buffer);
                session.addData(buffer, bytesRead);
                while(bytesRead == BUFFER_SIZE) {
                    buffer.clear();
                    bytesRead = socketChannel.read(buffer);
                    if(bytesRead > 0) {
                        //session.handleRequest(buffer);
                        session.addData(buffer, bytesRead);
                    }
                }
            }
            if(bytesRead < 0) {
                // no more data. Close the socket file channels
                session.close();
                sessions.remove(key);
            }
        }
    }
}