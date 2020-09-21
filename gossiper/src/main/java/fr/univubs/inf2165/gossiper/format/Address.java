package fr.univubs.inf2165.gossiper.format;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * This class defines the Address of a host. It contains the ip address (array
 * of 4 bytes) and the port number.
 *
 * @author Aliyou Sylla
 * @version 1.0.0
 */
public class Address {

    /**
     * The IP address size in bytes
     */
    private static final int IP_SIZE   = 4;
    /**
     * The port number size in bytes
     */
    private static final int PORT_SIZE = 2;
    /**
     * The host address size in bytes
     */
    public  static final int SIZE      = IP_SIZE + PORT_SIZE;

    private Inet4Address ip;
    private short port;

    /**
     * Constructs a new Address object with data read from the given buffer.
     *
     * @param buffer The buffer from which address data are read. Must not be null.
     */
    public Address(ByteBuffer buffer) throws UnknownHostException {
        Util.checkNotNull("buffer", buffer);
        byte[] ipBytes = new byte[IP_SIZE];
        this.ip = (Inet4Address) Inet4Address.getByAddress(ipBytes);
        this.port = buffer.getShort();
    }

    /**
     * Constructs new Address object with the hostname and the port number.
     *
     * @param host The hostname. Must not be null.
     * @param port The port number.
     */
    public Address(String host, short port) throws IOException {
        Util.checkNotNull("host", host);
        this.ip = (Inet4Address) Inet4Address.getByName(host);
        this.port = port;
    }

    /**
     * Writes the address data in the given buffer from the buffer current position.
     *
     * @param buffer The buffer in which data are written.
     */
    public void writeData(ByteBuffer buffer) {
        if(buffer.remaining() >= SIZE) {
            buffer.put(this.ip.getAddress());
            buffer.putShort(this.port);
        } else {
            System.err.println("Address data not written in the buffer");
        }
    }

    /**
     * Return the InetSocketAddress corresponding to the address
     * @return
     * @throws UnknownHostException
     */
    public InetSocketAddress getInetSocketAddress() throws UnknownHostException {
        return new InetSocketAddress(this.ip, this.port);
    }

    /**
     * Return the IP address.
     * @return the IP address.
     */
    public Inet4Address getIp() {
        return this.ip;
    }

    /**
     * Set a new ip address.
     */
    public void setIp(Inet4Address ip) {
        if(ip != null) {
            this.ip = ip;
        }
    }

    /**
     * Return the port number.
     * @return the port number.
     */
    public short getPort() {
        return this.port;
    }

    /**
     * Set a new port number.
     */
    public void setPort(short port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "ip=" + this.ip.getHostAddress() +" | port="+ this.port;
    }
}