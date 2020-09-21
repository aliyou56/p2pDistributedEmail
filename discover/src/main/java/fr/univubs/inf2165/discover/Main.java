package fr.univubs.inf2165.discover;

import java.io.IOException;
import java.net.Inet4Address;
import java.nio.file.Path;
import java.nio.file.Paths;

import fr.univubs.inf2165.gossiper.Gossiper;

/**
 * Main
 */
public class Main {

    /**
     * Prints how to use the program.
     */
    private static void usage() {
        System.out.println("Usage: Discover <multicast Address> <multicast port> <username> <baseDirectory> <UDPport> <TCPport> [IP] [delay]");
        System.out.println("With:");
        System.out.println("\t multicast address:  multicast address");
        System.out.println("\t multicast port:     multicast port");
        System.out.println("\t username:           user name");
        System.out.println("\t base_directory:     directory that contains user directories");
        System.out.println("\t UDPport:            local UDP port number");
        System.out.println("\t TCPport:            local TCP port number for receiving files");
        System.out.println("\t IP                  local IP address - localhost by default");
        System.out.println("\t delay               delay (seconds) before announcing it's presence - 5 seconds by default");
        System.exit(-1);
    }

    /**
     * Main
     *
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {

        if (args.length < 6 || "-h".equals(args[0].trim().toLowerCase())) {
            usage();
        }

        String groupAddress = args[0];
        short groupPort = Short.valueOf(args[1]);
        String username = args[2];
        Path baseDirectory = Paths.get(args[3]);
        short udpPort = Short.valueOf(args[4]);
        short tcpPort = Short.valueOf(args[5]);

        try {
            Gossiper gossiper;
            if(args.length > 6) {
                String ip = args[6];
                gossiper = new Gossiper(username, baseDirectory, udpPort, tcpPort, ip);
            } else {
                gossiper = new Gossiper(username, baseDirectory, udpPort, tcpPort);
            }
            gossiper.start();

            Thread.sleep(1000);

            int delay = (args.length > 7) ? Integer.parseInt(args[7]) : 5;
            Discover discover = new Discover(groupAddress, groupPort, gossiper, delay);
            discover.run();

        } catch(IOException | InterruptedException | NumberFormatException ioe) {
            System.err.println(ioe.getMessage());
            usage();
        }
    }

}