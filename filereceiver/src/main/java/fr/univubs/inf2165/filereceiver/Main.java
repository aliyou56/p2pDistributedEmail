package fr.univubs.inf2165.filereceiver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static final boolean DEBUG = false;

    /**
     * Prints information about how to use the program.
     */
    public static void usage() {
        System.out.println("usage : FileReceiver <port> <directory> [IP]");
        System.out.println("with");
        System.out.println("\tport:      local TCP port");
        System.out.println("\tdirectory: base directory ");
        System.out.println("\tIP:        local IP - localhost by default");
        System.exit(-1);
    }

    /**
     * Main
     * @param args
     */
    public static void main(String[] args) {

        if(args.length < 2 || "-h".equals(args[0].trim().toLowerCase())) {
            usage();
        }

        try {
            int port = Integer.valueOf(args[0]);
            Path directory = Paths.get(args[1]);

            FileReceiver receiver;
            if(args.length > 2) {
                String ip = args[2];receiver = new FileReceiver(port, directory, ip);
            } else {
                receiver = new FileReceiver(port, directory);
            }
            receiver.run();
        } catch (IOException ioe) {
            System.err.println("Error while creating receiver: " + ioe.getMessage());
        } catch (NumberFormatException nfe) {
            System.err.println("Error while converting the port number: " + nfe.getMessage());
        }

    }

}
