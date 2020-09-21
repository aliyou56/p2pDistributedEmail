package fr.univubs.inf2165.gossiper.format;

import java.nio.ByteBuffer;

/**
 * This class represents a delete message format. It contains information
 * about the user who asks to delete a file and information about the file
 * to delete. @link{AbstractMessageFormat}
 *
 * <pre>
 *     Format:
 *                        -------------------------------
 *             message =  |   userInfo   |   fileInfo   |
 *                        -------------------------------
 *          byte ->               x              x
 * </pre>
 *
 * @author Aliyou Sylla
 * @version 1.0.0
 */
public class DeleteMessageFormat extends AbstractMessageFormat {

    /**
     * Information about the user
     */
    private UserInfo userInfo;
    /**
     * Information about the file to delete
     */
    private FileInfo fileInfo;

    /**
     * Constructor with a buffer that contains delete message data.
     * @param buffer The buffer form which message data are read.
     */
    public DeleteMessageFormat(ByteBuffer buffer) {
        super(buffer);
        this.userInfo = new UserInfo(buffer);
        this.fileInfo = new FileInfo(buffer);
    }

    /**
     * Constructor with the user information and the file information.
     *
     * @param userInfo The user information. Must not be null
     * @param fileInfo The file information. Must not be null
     */
    public DeleteMessageFormat(UserInfo userInfo, FileInfo fileInfo) {
        super(MessageType.DELETE);
        Util.checkNotNull("userinfo", userInfo);
        Util.checkNotNull("fileinfo", fileInfo);
        this.userInfo = userInfo;
        this.fileInfo = fileInfo;
    }

    /**
     * Write delete message data in the given buffer from the buffer current position.
     * @param buffer The buffer in which data are written.
     */
    public void writeData(ByteBuffer buffer) {
        this.userInfo.writeData(buffer);
        this.fileInfo.writeData(buffer);
    }

    /**
     * Return the minimum size of a delete message. That is useful
     * when reading data from a buffer in order to check that there is the minimum
     * amount of data in the buffer before start the reading operation.
     *
     * @return the minimum size of a delete message
     */
    public int getMinSize() {
        return super.getMinSize() + UserInfo.MIN_SIZE + FileInfo.MIN_SIZE;
    }

    /**
     * Return the real size of a delete message. That allows to create
     * a buffer with the required size before writing data.
     *
     * @return the real size of a delete message.
     */
    public int getSize() {
        return super.getSize() + this.userInfo.getSize() + this.fileInfo.getSize();
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
        return super.toString() + " {" + this.userInfo + " | " + this.fileInfo + "}";
    }
}