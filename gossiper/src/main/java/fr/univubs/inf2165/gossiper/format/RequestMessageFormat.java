package fr.univubs.inf2165.gossiper.format;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * This class represents the request message format. @link{AbstractRequestOfferMessageFormat}
 *
 * <pre>
 *     Format:
 *                        -------------------------------
 *             address =  |   ip address   |  UDP port  |
 *                        -------------------------------
 *          byte  ->              4              2
 * </pre>
 *
 * @author Aliyou Sylla
 * @version 1.0.0
 */
public class RequestMessageFormat extends AbstractRequestOfferMessageFormat {

    /**
     * Constructor with a buffer containing request message data.
     * @param buffer The buffer from which request message data are read. Must not be null.
     */
    public RequestMessageFormat(ByteBuffer buffer) throws UnknownHostException {
        super(buffer);
    }

    /**
     * Constructor with the address, the user information and the file information.
     *
     * @param address The address. Must not be null
     * @param userInfo The user information. Must not be null
     * @param fileInfo The file information. Must not be null
     */
    public RequestMessageFormat(Address address, UserInfo userInfo, FileInfo fileInfo) {
        super(MessageType.REQUEST, address, userInfo, fileInfo);
    }
}