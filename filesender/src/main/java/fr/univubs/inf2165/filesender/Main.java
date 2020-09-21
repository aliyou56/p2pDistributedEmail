package fr.univubs.inf2165.filesender;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    /**
     * Prints information about how to use the program.
     */
    public static void usage() {
        System.out.println("usage : FileSender <host> <port> <filename>");
        System.exit(-1);
    }

    /**
     * Main
     * @param args Arguments
     */
    public static void main(String [] args) {

        if(args.length == 0 || args.length != 3 || "-h".equals(args[0].trim().toLowerCase())) {
            usage();
        }

        try {
            String host = args[0];
            int port = Integer.valueOf(args[1]);
            Path path = Paths.get(args[2]);

            try(FileSender sender = new FileSender(host, port)) {
                sender.sendFile(path);
            } catch(ConnectException ce) {
                System.err.println("Oops, Server is not running on -> " + host +":"+ port);
            } catch (IOException ioe) {
                System.err.println("I/O Error occurs : " + ioe.getMessage() );
            }

        } catch(NumberFormatException ex) {
            System.err.println("Error while converting the port number : " + ex.getMessage());
            usage();
        }
    }

}
