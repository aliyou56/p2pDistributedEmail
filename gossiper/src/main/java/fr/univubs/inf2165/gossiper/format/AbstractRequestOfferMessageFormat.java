
package fr.univubs.inf2165.gossiper.format;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * This class represents an abstract message format of a request or offer message.
 * It contains the address (which is the ip address and the tcp port in the case
 * of a request and the udp port in the case of an offer message), the user information
 * and the file information. @link{AbstractMessageFormat}
 *
 * <pre>
 *     Format:
 *                        --------------------------------------------
 *             message =  |   address   |   userInfo   |   fileInfo  |
 *                        --------------------------------------------
 *          byte  ->             6              x             x
 * </pre>
 *
 * @author Aliyou Sylla
 * @version 1.0.0
 */
public abstract class AbstractRequestOfferMessageFormat extends AbstractMessageFormat {

    private Address address;
    private UserInfo userInfo;
    private FileInfo fileInfo;

    /**
     * Constructor with a buffer that contains message (request/offer) data.
     * @param buffer The buffer form which message data are read.
     */
    protected AbstractRequestOfferMessageFormat(ByteBuffer buffer) throws UnknownHostException {
        super(buffer);
        this.address = new Address(buffer);
        this.userInfo = new UserInfo(buffer);
        this.fileInfo = new FileInfo(buffer);
    }

    /**
     * Constructor with the message type, the address, the user information and
     * the file information.
     *
     * @param messageType The message type. Must not be null
     * @param address The address. Must not be null
     * @param userInfo The user information. Must not be null
     * @param fileInfo The file information. Must not be null
     */
    protected AbstractRequestOfferMessageFormat(MessageType messageType, Address address, UserInfo userInfo, FileInfo fileInfo) {
        super(messageType);
        Util.checkNotNull("address", address);
        Util.checkNotNull("userinfo", userInfo);
        Util.checkNotNull("fileinfo", fileInfo);
        this.address = address;
        this.userInfo = userInfo;
        this.fileInfo = fileInfo;
    }

    /**
     * Writes request/offer message data in the given buffer from the buffer current position.
     * @param buffer The buffer in which data are written.
     */
    public void writeData(ByteBuffer buffer) {
        this.address.writeData(buffer);
        this.userInfo.writeData(buffer);
        this.fileInfo.writeData(buffer);
    }

    /**
     * Return the minimum size of a request/offer message. That is useful
     * when reading data from a buffer in order to check that there is the minimum
     * amount of data in the buffer before start the reading operation.
     *
     * @return the minimum size of a request/offer message
     */
    public int getMinSize() {
        return super.getMinSize() + Address.SIZE + UserInfo.MIN_SIZE + FileInfo.MIN_SIZE;
    }

    /**
     * Return the real size of a request/offer message. That allows to create
     * a buffer with the required size before writing data.
     *
     * @return the real size of a request/offer message.
     */
    public int getSize() {
        return super.getSize() + Address.SIZE + this.userInfo.getSize() + this.fileInfo.getSize();
    }

    /**
     * Return the address.
     * @return the address.
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

    /**
     * Return the file info.
     * @return the file info.
     */
    public FileInfo getFileInfo() {
        return this.fileInfo;
    }

    @Override
    public String toString() {
        return super.toString() + " {" + this.address +" | "+ this.userInfo +" | "+ this.fileInfo +"}";
    }
}