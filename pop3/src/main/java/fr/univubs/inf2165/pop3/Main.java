package fr.univubs.inf2165.pop3;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    /**
     * Prints how to use the program.
     */
    private static void usage() {
        System.out.println("Usage: POP3Server <port> <base_directory>");
        System.out.println("With:");
        System.out.println("\t port            local port number");
        System.out.println("\t base_directory  directory that contains mails");
        System.exit(-1);
    }

    /**
     * Main
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {

        if(args.length != 2) {
            usage();
        }

        try {
            int port = Integer.valueOf(args[0]);
            Path baseDirectory = Paths.get(args[1]);

            POP3Server server = new POP3Server(port, baseDirectory);
            server.run();
        } catch(NumberFormatException nfe) {
            System.err.println("Error while converting the port number : " + nfe.getMessage());
        }
    }

}
