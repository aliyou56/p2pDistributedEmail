package fr.univubs.inf2165.gossiper.format;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {
    /*
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }
    */
    @org.junit.jupiter.api.Test
    void writeData() {
        System.out.println("writeData");
        try {
            String ip = Inet4Address.getLocalHost().getHostName();
            short port = (short) 5000;
            Address instance = new Address(ip, port);
            ByteBuffer buffer = ByteBuffer.allocate(Address.SIZE);
            instance.writeData(buffer);
            buffer.flip();
            byte[] ipRead = new byte[4];
            buffer.get(ipRead);
            assertEquals(Inet4Address.getByAddress(ipRead), instance.getIp());
            assertEquals(buffer.getShort(), instance.getPort());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @org.junit.jupiter.api.Test
    void getInetSocketAddress() {
        System.out.println("getInetSocketAddress");
        try {
            String ip = Inet4Address.getLocalHost().getHostName();
            short port = (short) 5000;
            Address instance = new Address(ip, port);
            assertEquals(new InetSocketAddress(ip, port), instance.getInetSocketAddress());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /*@org.junit.jupiter.api.Test
    void getIp() {
        System.out.println("getIp");
        try {
            String ip = Inet4Address.getLocalHost().getHostName();
            short port = (short) 6000;
            Address instance = new Address(ip, port);
            assertEquals(ip, instance.getIp());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }*/

    /*@org.junit.jupiter.api.Test
    void setIp() {
        System.out.println("setIp");
        try {
            String ip = Inet4Address.getLocalHost().getHostName();
            short port = (short) 6000;
            Address instance = new Address(ip, port);

            byte[] newIp = InetAddress.getByName("10.10.10.10").getAddress();
            instance.setIp(newIp);

            assertEquals(newIp, instance.getIp());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }*/

    @org.junit.jupiter.api.Test
    void getPort() {
        System.out.println("getPort");
        try {
            String ip = Inet4Address.getLocalHost().getHostName();
            short port = (short) 60525;
            Address instance = new Address(ip, port);
            assertEquals(port, instance.getPort());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @org.junit.jupiter.api.Test
    void setPort() {
        System.out.println("setPort");
        try {
            String ip = Inet4Address.getLocalHost().getHostName();
            short port = (short) 6000;
            Address instance = new Address(ip, port);

            short newPort = (short) 5000;
            instance.setPort(newPort);

            assertEquals(newPort, instance.getPort());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}