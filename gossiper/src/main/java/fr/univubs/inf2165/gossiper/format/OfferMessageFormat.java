package fr.univubs.inf2165.gossiper.format;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * This class represents the offer message format. @link{AbstractRequestOfferMessageFormat}
 *
 * <pre>
 *     Format:
 *                        -------------------------------
 *             address =  |   ip address   |  TCP port  |
 *                        -------------------------------
 *          byte  ->              4              2
 * </pre>
 *
 * @author Aliyou Sylla
 * @version 1.0.0
 */
public class OfferMessageFormat extends AbstractRequestOfferMessageFormat {

    /**
     * Constructor with a buffer containing offer message data.
     * @param buffer The buffer from which offer message data are read. Must not be null.
     */
    public OfferMessageFormat(ByteBuffer buffer) throws UnknownHostException {
        super(buffer);
    }

    /**
     * Constructor with the address, the user information and the file information.
     *
     * @param address The address. Must not be null
     * @param userInfo The user information. Must not be null
     * @param fileInfo The file information. Must not be null
     */
    public OfferMessageFormat(Address address, UserInfo userInfo, FileInfo fileInfo) {
        super(MessageType.OFFER, address, userInfo, fileInfo);
    }
}