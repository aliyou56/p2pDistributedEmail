package fr.univubs.inf2165.filereceiver;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.concurrent.ExecutionException;

import fr.ubs.io.MailFile;

/**
 * This program receives files send by clients. Files are stored in the
 * given directory path.
 *
 * @author Aliyou Sylla
 * @version 08/10/2019
 */
public class FileReceiver extends Thread {

    /**
     * The port number
     */
    private int port;
    /**
     * The base directory. It contains received files.
     */
    private Path baseDirectory;

    private String ip;

    /**
     * Constructor with the port number and the base directory.
     *
     * @param port          The port number on which server listens.
     * @param baseDirectory The base directory, contains received files.
     * @param ip            The local IP
     */
    public FileReceiver(int port, Path baseDirectory, String ip) throws IOException {
        this(port, baseDirectory);
        this.ip = ip;
    }

    /**
     * Constructor with the port number and the base directory.
     *
     * @param port          The port number on which server listens.
     * @param baseDirectory The base directory, contains received files.
     */
    public FileReceiver(int port, Path baseDirectory) throws IOException {
        this.port = port;
        this.baseDirectory = baseDirectory;
        if (!Files.isDirectory(this.baseDirectory)) {
            Files.createDirectories(this.baseDirectory);
            System.out.println("[FileReceiver]: I've just created the directory -> " + this.baseDirectory.toString());
        }
        this.ip = Inet4Address.getLocalHost().getHostName();
    }

    @Override
    public void run() {
        try (final AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open()) {
            InetSocketAddress inetAddress = new InetSocketAddress(this.ip, this.port);
            serverChannel.bind(inetAddress);
            System.out.println("[FileReceiver]: hey ! I'm running on " + serverChannel.getLocalAddress());
            System.out.println("[FileReceiver]: baseDirectory -> " + this.baseDirectory);

            while (true) {
                serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

                    @Override
                    public void completed(AsynchronousSocketChannel ch, Object attachement) {
                        try {
                            if (serverChannel.isOpen()) {
                                System.out.println("[FileReceiver]: incoming connection from -> " + ch.getRemoteAddress());
                                serverChannel.accept(null, this);
                            }
                            handle(ch, baseDirectory);
                        } catch (IOException e) {
                            System.err.println("[FileReceiver]: input/output error -> " + e.getMessage());
                            //usage();
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Object attachement) {
                        System.err.println("[FileReceiver]: Failed to accept connection");
                    }
                });
                System.in.read();
            }
        } catch (IOException ioe) {
            System.err.println("[FileReceiver]: I/O Error occurs -> " + ioe.getMessage());
        }
    }

    /**
     * Handle a client connection.
     *
     * @param clientChannel
     * @param directory
     * @throws IOException
     */
    private void handle(AsynchronousSocketChannel clientChannel, Path directory) throws IOException {
        try {
            String filename = clientChannel.getRemoteAddress()
                    .toString().substring(1).replace(".", ",").replace(":", "-") + "--" + System.currentTimeMillis();
            Path path = directory.resolve(filename);
            long bytesReceived = receiveFile(clientChannel, path);
            MailFile mailFile = new MailFile(path.toFile());
            if (mailFile.getMessageId() != null) {
                mailFile.updateFilename();
                filename = mailFile.getMessageId();
            }
            System.out.println("[FileReceiver]: file \'" + filename
                    + "\' successfully received -> " + bytesReceived + " bytes received !");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Receives data from a given channel and write it to a file.
     *
     * @param clientChannel The client channel
     * @param path
     * @return the number of bytes received..
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private static long receiveFile(AsynchronousSocketChannel clientChannel, Path path)
            throws IOException, ExecutionException, InterruptedException {
        long bytesReceived = 0;
        try (FileChannel inChannel = FileChannel.open(path,
                EnumSet.of(StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE))
        ) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (clientChannel.read(buffer).get() != -1) {
                buffer.flip();
                bytesReceived += buffer.limit();
                inChannel.write(buffer);
                buffer.clear();
            }
        }
        return bytesReceived;
    }

}