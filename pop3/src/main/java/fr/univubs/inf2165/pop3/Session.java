
package fr.univubs.inf2165.pop3;

import static fr.univubs.inf2165.pop3.POP3Server.DEBUG;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import fr.ubs.io.LockableDirectory;
import fr.ubs.io.MailFile;

/**
 * This class manages a client session.
 *
 * @author Aliyou Sylla
 * @version 14/10/2019
 */
public class Session implements Closeable {

    private SocketChannel socketChannel;
    private boolean closed = true;

    private Path baseDirectory;
    /**
     * user maildrop
     */
    private Path maildrop = null;
    private String user = null, password = null;

    private StringBuilder builder;

    private State state = null;
    private String error; // error message

    private HashMap<Integer, MailFile> mails;
    private Set<Integer> deletedMessages;

    private LockableDirectory lockableUserDirectory = null;

    /**
     * Constructor with the client socket channel and the base directory.
     *
     * @param socketChannel the socket channel
     * @param baseDirectory Base directory
     */
    public Session(SocketChannel socketChannel, Path baseDirectory) {
        this.socketChannel = socketChannel;
        this.baseDirectory = baseDirectory;
        this.builder = new StringBuilder();
        this.mails = new HashMap<>();
        this.deletedMessages = new HashSet<>();
    }

    /**
     * Open a session with a client.
     * @throws IOException
     */
    public void open() throws IOException {
        this.closed = false;
        sendReply("+OK POP3 server ready");
        this.state = State.AUTHORIZATION;
        System.out.println("S: Session opened");
    }

    @Override
    public synchronized void close() {
        if( ! this.closed) {
            this.closed = true;
            try {
                this.socketChannel.close();
                System.out.println("S: Session closed");
            } catch(IOException ioe) {
                System.err.println("Error while closing client channel: " + ioe.getMessage());
            }
            unlock();
        }
    }

    public void unlock() {
        if(this.lockableUserDirectory != null) {
            this.lockableUserDirectory.releaseLock();
        }
        this.lockableUserDirectory = null;
    }

    public void lock() throws IOException {
        if(this.maildrop != null) {
            if(this.maildrop.toFile().exists() && this.maildrop.toFile().isDirectory()) {
                this.lockableUserDirectory = new LockableDirectory(this.maildrop.toFile());
                this.lockableUserDirectory.acquireLock();
            }
        }
    }

    /**
     * Add data read from the client socket and process the request
     * if it contains a CRLF mark
     *
     * @param buffer the buffer that contains the data read
     * @param length the number of bytes read
     */
    public void addData(ByteBuffer buffer, int length) {
        String str = getString(buffer, length);

        // look for CRLF in the String object
        int idx = getEOLIndex(str);
        if (idx >= 0) {

            // complete the builder and adds the line to the queue
            process(builder.append(str.substring(0, idx)).toString());
            builder = new StringBuilder();
            str = remaining(str, idx);

            // process the remaining characters in the String object
            idx = getEOLIndex(str);
            while (idx >= 0) {
                process(str.substring(0, idx));
                str = remaining(str, idx);
                idx = getEOLIndex(str);
            }
        }

        // concatenates the remaining characters to the builder
        if (str.length() > 0) {
            builder.append(str);
        }
    }

    /**
     * Give the index of the first EOL character (\n or \r\n) in the
     * given text
     *
     * @param str a text
     * @return the index of the first EOL character or -1 if there is not
     */
    private int getEOLIndex(String str) {
        int idx = str.indexOf("\r\n");
        return (idx >= 0) ? idx : str.indexOf("\n");
    }

    /**
     * Give the remaining text after the EOL character at the given index
     *
     * @param str a text
     * @param idx the index of the first EOL character (\n or \r\n)
     * @return the remaining text after the EOL
     */
    private String remaining(String str, int idx) {
        if (str.charAt(idx) == '\r') {
            return idx < str.length()-2 ? str.substring(idx+2) : "";
        }
        return idx < str.length()-1 ? str.substring(idx+1) : "";
    }

    private void process(String line) {
        int errorCode = 1;
        try {
            error = "UNKNOWN COMMAND"; // default error message

            if (DEBUG) {
                System.out.println("C: " + line);
            }

            String[] command = line.split(" ");
            if(command.length > 0) {
                try {
                    Keyword keyword = Keyword.valueOf(command[0].toUpperCase());
                    error = "UNKNOWN COMMAND: " + keyword;

                    if(keyword == Keyword.QUIT) {
                        errorCode = quit();
                        close();
                    } else if( (this.state == State.AUTHORIZATION) &&
                            ! State.getKeywords(State.AUTHORIZATION).contains(keyword)) {
                        errorCode = -1;
                        this.error = "-ERR you need to identify yourself first.";
                    } else {
                        switch(this.state) {
                            case AUTHORIZATION: {
                                if(State.getKeywords(State.AUTHORIZATION).contains(keyword)) {
                                    if(command.length > 1) {
                                        if(keyword == Keyword.USER) {
                                            errorCode = user(command);
                                        } else { // PASS
                                            errorCode = pass(command);
                                        }
                                    } else {
                                        syntaxError();
                                    }
                                }
                                break;
                            }

                            case TRANSACTION: {
                                if(State.getKeywords(State.TRANSACTION).contains(keyword)) {
                                    switch(keyword) {
                                        case STAT: {
                                            errorCode = (command.length == 1) ? stat(command) : syntaxError();
                                            break;
                                        }
                                        case LIST: {
                                            errorCode = (command.length <= 2) ? list(command) : syntaxError();
                                            break;
                                        }
                                        case RETR: {
                                            errorCode = (command.length == 2) ? retr(command) : syntaxError();
                                            break;
                                        }
                                        case DELE: {
                                            errorCode = (command.length == 2) ? dele(command) : syntaxError();
                                            break;
                                        }
                                        case NOOP: {
                                            errorCode = (command.length == 1) ? noop(command) : syntaxError();
                                            break;
                                        }
                                        default:   {
                                            errorCode = (command.length == 1) ? rset(command) : syntaxError();
                                        }
                                    }
                                }
                                break;
                            }

                            default :
                        }
                    }
                } catch(IllegalArgumentException e) {
                    errorCode = -1;
                }
            }

            if(errorCode != 0) { // send error message
                sendReply(error);
            }
        } catch(IOException e) {
            close();
        }
    }

    private int syntaxError() {
        this.error = "SYNTAX ERROR";
        return 101;
    }

    /**
     * Send a reply to the client.
     * @param reply reply
     */
    private void sendReply(String reply) {
        try {
            ByteBuffer buffer = getByteBuffer(reply + "\n");
            int nbBytes = this.socketChannel.write(buffer);
            while(nbBytes < buffer.capacity()) {
                nbBytes += this.socketChannel.write(buffer);
            }
            if (DEBUG) {
                System.out.println("S: " + reply);
            }
        } catch(IOException ioe) {
            close();
            ioe.printStackTrace(System.out);
        }
    }

    private int quit() {
        int errorCode = 0;
        if(this.state == State.TRANSACTION) {
            this.state = State.UPDATE;
            for(Integer messageNumber : deletedMessages) { 
                MailFile mailFile = this.mails.get(messageNumber);
                if( ! mailFile.getFile().delete()) {
                    errorCode = 1;
                    this.error = "-ERR some deleted messages not removed";
                }
            }
        }
        if(errorCode == 0) {
            sendReply("+OK see you later !");
            //System.out.println("S: session closed");
        }
        return errorCode;
    }

    private int user(String[] command) {
        int errorCode = 0;
        this.user = command[1];
        this.maildrop = this.baseDirectory.resolve(this.user);
        //System.out.println("maildrop : " + this.maildrop);
        if(this.maildrop.toFile().exists()) {
            sendReply("+OK welcome " + this.user);
        } else {
            errorCode = 1;
            this.error = "-ERR sorry, no mailbox for " + this.user +" here";
        }
        return errorCode;
    }

    private int pass(String[] command) {
        int errorCode = 0;
        if(this.user != null) {
            this.password = command[1];
            sendReply("+OK connection established");
            this.state = State.TRANSACTION;
            try {
                lock();
                int number = 0;
                for(File file : this.maildrop.toFile().listFiles()) {
                    MailFile mailFile = new MailFile(file);
                    if(mailFile.getMessageId() != null) {
                        number++;
                        mails.put(number, mailFile);
                    }
                }
            } catch(IOException ioe) {
                errorCode = 22;
                System.err.println("S: Input out error : " + ioe.getMessage());
            }
        } else {
            errorCode = 2;
            this.error = "-ERR unknow user";
        }
        return errorCode;
    }

    private int stat(String[] command) { // drop listings
        long size = 0;
        int nb = this.mails.size() - this.deletedMessages.size();
        for(MailFile mailfile : this.mails.values()) {
            size += mailfile.getFile().length();
        }
        for(int messageNumber : deletedMessages) {
            if(this.mails.containsKey(messageNumber)) {
                size -= this.mails.get(messageNumber).getFile().length();
            }
        }
        sendReply("+OK "+ nb +" ("+ size +")");
        return 0;
    }

    private int list(String[] command) {
        int errorCode = 0;
        int messageNumber = command.length > 1 ? getMessageNumber(command[1]) : -1;
        if(messageNumber != -1) {
            if(this.mails.containsKey(messageNumber)) {
                long size = this.mails.get(messageNumber).getFile().length();
                sendReply("+OK "+ messageNumber +" "+ size);
            } else {
                errorCode = 3;
                this.error = "-ERR no such message";
            }
        } else {
            if( ! this.mails.isEmpty()) {
                sendReply("+OK "+ this.mails.size() +" messages");
                for(Integer key : this.mails.keySet()) {
                    String reply = key + " " + this.mails.get(key).getFile().length();
                    sendReply(reply);
                }
            } else {
                sendReply("+OK no scan listings--");
            }
            sendReply(".");
        }
        return errorCode;
    }

    private int getMessageNumber(String arg) {
        try {
            return Integer.valueOf(arg);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    private int retr(String[] command) throws IOException {
        int errorCode = 0;
        int messageNumber = getMessageNumber(command[1]);
        // messageId exits and not refer to deleted message
        if(this.mails.containsKey(messageNumber) && ! this.deletedMessages.contains(messageNumber)) {
            MailFile mailFile = this.mails.get(messageNumber);
            sendReply("+OK " + mailFile.getFile().length());

            try (BufferedReader reader = new BufferedReader(new FileReader(mailFile.getFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if(line.startsWith(".")) {
                        line = "." + line;
                    }
                    sendReply(line.replace("\\n", ""));
                }
            }

            sendReply(".");
        } else {
            errorCode = 4;
            this.error = "-ERR no such message";
        }
        return errorCode;
    }

    private int dele(String[] command) {
        int errorCode = 0;
        int messageNumber = getMessageNumber(command[1]);
        if(this.mails.containsKey(messageNumber)) {
            if( ! this.deletedMessages.contains(messageNumber)) {
                this.deletedMessages.add(messageNumber);
                sendReply("+OK message deleted");
            } else {
                errorCode = 5;
                this.error = "-ERR message "+messageNumber+" already deleted";
            }
        } else {
            errorCode = 6;
            this.error = "-ERR no such message";
        }
        return errorCode;
    }

    private int noop(String[] command) {
        sendReply("+OK");
        return 0;
    }

    private int rset(String[] command) {
        int size = this.deletedMessages.size();
        this.deletedMessages.clear();
        sendReply("+OK " +size+ " message(s) unmarked");
        return 0;
    }

    /**
     * Return a String read from the given buffer at the position 0.
     * @param buffer Buffer
     * @param length Length of the string
     * @return a String read from the given buffer at the position 0.
     */
    private String getString(ByteBuffer buffer, int length) {
        buffer.flip();
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes);
    }

    /**
     * Return a buffer containing the given string.
     *
     * @param str The string to be put in a buffer
     * @return a buffer containing the given string.
     */
    private ByteBuffer getByteBuffer(String str) {
        byte[] bytes = new String(str).getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }

}