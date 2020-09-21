package fr.univubs.inf2165.gossiper.format;

import java.nio.ByteBuffer;

/**
 * This is the abstract format of exchanged messages
 *
 * <pre>
 *     Format:
 *              --------------------------
 *              |   code   |   message   |
 *              --------------------------
 *     byte ->       1           x
 * </pre>
 *
 * @author Aliyou Sylla
 * @version 21/09/2019
 */
public abstract class AbstractMessageFormat {

    /**
     * The size of the message type in bytes
     */
    private static final int SIZE = 1;

    /**
     * The message type.
     */
    private MessageType messageType;

    /**
     * Constructor with a buffer containing message data. Starts reading from
     * the buffer current position.
     * @param buffer The buffer containing message data. Must not be null.
     */
    protected AbstractMessageFormat(ByteBuffer buffer) {
        Util.checkNotNull("buffer", buffer);
        //System.out.println("buffer : " + buffer);
        if(buffer.remaining() < this.getMinSize()) {
            throw new IllegalArgumentException("Data can't be read from the buffer \n\t cause -> not enough data in the buffer");
        }
        MessageType messageType = MessageType.getMessageType(buffer.get());
        Util.checkNotNull("messageType", messageType);
        this.messageType = messageType;
    }

    /**
     * Constructor with the message type.
     * @param messageType The message type. Must not be null
     */
    protected AbstractMessageFormat(MessageType messageType) {
        Util.checkNotNull("messageType", messageType);
        this.messageType = messageType;
    }

    /**
     * Return a buffer containing message data.
     * @return
     */
    public ByteBuffer getPacket() {
        ByteBuffer buffer = ByteBuffer.allocate(this.getSize());
        buffer.put(this.messageType.getCode());
        this.writeData(buffer);
        buffer.flip();
        return buffer;
    }

    /**
     * Writes the message data in the given buffer.
     * @param buffer The buffer in which data are written.
     */
    public abstract void writeData(ByteBuffer buffer);

    /**
     * Return the minimun size of a message format.
     * @return the minimun size of a message format.
     */
    public int getMinSize() {
        return AbstractMessageFormat.SIZE;
    }

    /**
     * Return the real size of a message format.
     * @return the real size of a message format.
     */
    public int getSize() {
        return AbstractMessageFormat.SIZE;
    }

    /**
     * Return the message type.
     * @return the message type.
     */
    public MessageType getMessageType() {
        return this.messageType;
    }

    /**
     * Set a new message type.
     * @param messageType the new message type.
     */
    /*public void setMesssageType(MessageType messageType) {
        if(messageType != null) {
            this.messageType = messageType;
        }
    }*/

    @Override
    public String toString() {
        return "Message [type=" + this.messageType + "]";
    }
}
