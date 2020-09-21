package fr.univubs.inf2165.gossiper;

import fr.univubs.inf2165.gossiper.format.Address;
import fr.univubs.inf2165.gossiper.format.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class manages gossiping session through the user input from the
 * standard input.
 *
 * @authoe aliyou sylla
 * @version 1.0.1
 */
public class UserHandlingSession implements Runnable {

    private Gossiper gossiper; // the gossiper server

    private BufferedReader keyboard; // the keyboard

    /**
     * Constructor with the gossiper server
     * @param gossiper The gossiper server
     */
    public UserHandlingSession(Gossiper gossiper) {
        Util.checkNotNull("UserHandlingSession -> gossiper", gossiper);
        this.gossiper = gossiper;
        this.keyboard = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * Prints how to open a session and stop the gossiping server
     */
    private static void sessionOpenHelp() {
        System.out.println("\nTo open a gossiping session, enter: <host> <port>");
        System.out.println("\t <host>:     the host name of the peer");
        System.out.println("\t <port>:     the UDP port number of the peer");
        System.out.println("To stop the gossiping server, enter: stop\n");
    }

    @Override
    public void run() {
        sessionOpenHelp();
        boolean stop = false;
        String line;
        while ( ! stop ) {
            //System.out.print(">> ");
            try {
                line = keyboard.readLine();
                //System.out.println("line = " + line);
                String[] values = line.split(" ");
                if(values.length == 2) {
                    String host = values[0];
                    short port = Short.valueOf(values[1]);
                    Address peerAddress = new Address(host, port);
                    try(GossipingSession session = new GossipingSession(gossiper, peerAddress)) {
                        session.run(); // starts new session with a peer
                    }
                } else if ("stop".equals(line.trim().toLowerCase())) {
                    stop = true;
                    gossiper.stopServer();
                } else { // open a session first
                    sessionOpenHelp();
                }
            } catch(NumberFormatException nfe) {
                System.err.println("UserHandlingSession -> the given port number is not correct: " + nfe.getMessage());
                sessionOpenHelp();
            } catch (IOException ioex) {
                System.err.println("UserHandlingSession -> I/O error : " + ioex.getMessage());
                sessionOpenHelp();
            }
        }
    }











    /**
     * Prints a help message for the session usage.
     */
    /*public static void sessionCommandHelp() {
        System.out.println("\nCommand syntax: ");
        System.out.println(" \t<command=[OFFER | REQUEST | DELETE]> <filename>");
        System.out.println("To close the session, enter: close\n");
    }*/

    /*
    @Override
    public void run() { /*sessionOpenHelp();
        boolean stop = false;
        String line;
        while ( ! stop ) {
            System.out.print(">> ");
            try {
                line = keyboard.readLine();
                //System.out.println("line = " + line);
                if (line.toLowerCase().startsWith("session")) {
                    String[] values = line.split(" ");
                    if (values.length == 3) {
                        try {
                            String host = values[1];
                            short port = Short.valueOf(values[2]);
                            Address peerAddress = new Address(host, port);

                            //startSession(peerAddress); // starts new session with a peer

                        } catch (NumberFormatException nfe) {
                            System.err.println("The given port is not correct : " + nfe.getMessage());
                        } catch (IOException ioex) {
                            System.err.println("The given peer address is not correct : " + ioex.getMessage());
                        }
                    } else {
                        System.out.println("syntax error");
                        sessionOpenHelp();
                    }
                } else if ("stop".equals(line.trim().toLowerCase())) {
                    stop = true;
                    gossiper.stopServer();
                } else { // open a session first
                    sessionOpenHelp();
                }
            } catch (IOException ioex) {
                System.err.println("I/O error : " + ioex.getMessage());
            }
        }
    }
    */
    /*
    private void startSession(Address peerAddress) {
        try (GossipingSession session = new GossipingSession(this.gossiper, peerAddress)) {
            boolean closed = false; // if true the session is closed.
            String command;
            sessionCommandHelp();
            while( ! closed ) {
                System.out.print("["+peerAddress+"]: ");
                try {
                    if ((command = keyboard.readLine()) != null) {
                        //System.out.println("command = " + command);
                        String[] commands = command.split(" ");
                        if (commands.length == 2) {
                            try {
                                MessageType messageType = MessageType.valueOf(commands[0].toUpperCase());
                                String filename = commands[1];
                                session.send(messageType, filename);
                            } catch (IllegalArgumentException iaex) {
                                System.out.println(" unknown command: " + commands[0]);
                                sessionCommandHelp();
                            }
                        } else if ("close".equals(command.trim().toLowerCase())) { // close the session
                            closed = true;
                        } else {
                            sessionCommandHelp();
                        }
                    }
                } catch (IOException ioex) {
                    System.err.println("Error while reading from keyboard : " + ioex.getMessage());
                }
            }
        } catch (IOException ioex) {
            System.err.println("I/O error: " + ioex.getMessage());
        }
    }
    */

}