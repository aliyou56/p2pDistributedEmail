
package fr.univubs.inf2165.gossiper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main
 */
public class Main {

    /**
     * Prints how to use the program.
     */
    private static void usage() {
        System.out.println("Usage: Gossiper <username> <baseDirectory> <UDPport> <TCPport> [ip]");
        System.out.println("With:");
        System.out.println("\t username        user name");
        System.out.println("\t base_directory  directory that contains user directories");
        System.out.println("\t UDPport         local UDP port number");
        System.out.println("\t TCPport         local TCP port number for receiving files");
        System.out.println("\t IP              local IP address - localhost by default");
        System.exit(-1);
    }

    /**
     * Main
     *
     * @param args The arguments of the program.
     */
    public static void main(String[] args) throws InterruptedException {

        if (args.length < 4 || "-h".equals(args[0].trim().toLowerCase())) {
            usage();
        }

        try {

            String username = args[0];
            Path baseDirectory = Paths.get(args[1]);
            short udpPort = Short.valueOf(args[2]);
            short tcpPort = Short.valueOf(args[3]);

            Gossiper gossiper;
            if(args.length > 4) {
                String ip = args[4];
                gossiper = new Gossiper(username, baseDirectory, udpPort, tcpPort, ip);
            } else {
                gossiper = new Gossiper(username, baseDirectory, udpPort, tcpPort);
            }
            gossiper.start();

            Thread.sleep(1000); // just to make help message print after the running message

            UserHandlingSession userHandlingSession = new UserHandlingSession(gossiper);
            userHandlingSession.run();

        } catch(IOException ioe) {
            System.err.println("Error while creating the gossiper server: " + ioe.getMessage());
        } catch(NumberFormatException nfe) {
            System.err.println("A given port number is not correct: " + nfe.getMessage());
            usage();
        }

    }
}