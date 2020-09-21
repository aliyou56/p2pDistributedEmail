package fr.univubs.inf2165.gossiper.format;

/**
 * This is an enumeration of message type allowed.
 *
 * @author aliyou sylla
 * @version 1.0.1
 */
public enum MessageType {

    /**
     * The offer message type
     */
    OFFER((byte) 1),

    /**
     * The request message type
     */
    REQUEST((byte) 2),

    /**
     * The delete message type
     */
    DELETE((byte) 3),

    /**
     * The beacon message type
     */
    BEACON((byte) 4);

    private byte code;

    MessageType(byte code) {
        this.code = code;
    }

    /**
     * Return the code of the message type
     * @return the code of the message type
     */
    public byte getCode() {
        return this.code;
    }

    /**
     * Return the corresponding message type to the given code if exists
     * null otherwise.
     * @param code The message type code
     * @return The corresponding message type to the given code.
     */
    public static MessageType getMessageType(byte code) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.getCode() == code) {
                return messageType;
            }
        }
        return null;
    }
}