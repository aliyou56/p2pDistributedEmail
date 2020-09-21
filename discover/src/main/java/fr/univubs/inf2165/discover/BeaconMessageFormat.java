package fr.univubs.inf2165.discover;

import fr.univubs.inf2165.gossiper.format.*;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * This class represents a beacon message format. It contains information
 * about the address and the user who sends the beacon message. @link{AbstractMessageFormat}
 *
 * <pre>
 *     Format:
 *                        -------------------------------
 *             message =  |   address   |   userInfo   |
 *                        -------------------------------
 *          byte ->             6             x
 * </pre>
 *
 * @author aliyou sylla
 * @version 1.0.0
 */
public class BeaconMessageFormat extends AbstractMessageFormat {

    private Address address;
    /**
     * Information about the user
     */
    private UserInfo userInfo;

    /**
     * Constructor with a buffer that contains beacon message data.
     * @param buffer The buffer form which message data are read.
     */
    public BeaconMessageFormat(ByteBuffer buffer) throws UnknownHostException {
        super(buffer);
        this.address = new Address(buffer);
        this.userInfo = new UserInfo(buffer);
    }

    /**
     * Constructor with the address and the file information.
     *
     * @param address The address. Must not be null
     * @param userInfo The user information. Must not be null
     */
    public BeaconMessageFormat(Address address, UserInfo userInfo) {
        super(MessageType.BEACON);
        Util.checkNotNull("BeaconMessageFormat -> address", address);
        Util.checkNotNull("BeaconMessageFormat -> userinfo", userInfo);
        this.address = address;
        this.userInfo = userInfo;
    }


    /**
     * Write beacon message data in the given buffer from the buffer current position.
     * @param buffer The buffer in which data are written.
     */
    public void writeData(ByteBuffer buffer) {
        this.address.writeData(buffer);
        this.userInfo.writeData(buffer);
    }

    /**
     * Return the minimum size of a beacon message. That is useful
     * when reading data from a buffer in order to check that there is the minimum
     * amount of data in the buffer before start the reading operation.
     */
    public int getMinSize() {
        return super.getMinSize() + Address.SIZE + UserInfo.MIN_SIZE;
    }

    /**
     * Return the real size of a beacon message. That allows to create
     * a buffer with the required size before writing data.
     *
     * @return the real size of a beacon message.
     */
    public int getSize() {
        return super.getSize() + Address.SIZE + this.userInfo.getSize();
    }

    /**
     * Return the address.
     * @return the user address.
     */
    public Address getAddress() {
        return this.address;
    }

    /**
     * Return the user info.
     * @return the user info.
     */
    public UserInfo getUserInfo() {
        return this.userInfo;
    }

    @Override
    public String toString() {
        return super.toString() + " {" + this.address + " | " + this.userInfo + "}";
    }
}