package fr.univubs.inf2165.gossiper.format;

import java.nio.ByteBuffer;

/**
 *
 *
 * <pre>
 *     Format:
 *                ----------------------
 *                |  n  |     data     |
 *                ----------------------
 *     byte ->       1     x [1, 256]
 * </pre>
 *
 * @author Aliyou Sylla
 * @version 1.0.0
 */
public abstract class AbstractInfo {

    /**
     * The size of the data length (N) in bytes
     */
    private static final int N_SIZE        = 1;
    /**
     * The minimum size of the data in bytes
     */
    private static final int DATA_MIN_SIZE = 1;
    /**
     * The maximum size of the data in bytes
     */
    private static final int DATA_MAX_SIZE = 256;

    /**
     * The minimum size of the abstract info in bytes
     */
    public static final int MIN_SIZE = N_SIZE + DATA_MIN_SIZE;
    /**
     * The maximum size of the abstract info in bytes
     */
    public static final int MAX_SIZE = N_SIZE + DATA_MAX_SIZE;

    private String data;

    /**
     * Constructor with a buffer.
     * @param buffer The buffer from which data are read. Must not be null.
     */
    protected AbstractInfo(ByteBuffer buffer) {
        Util.checkNotNull("buffer", buffer);
        byte n = buffer.get();
        if(buffer.remaining() < n) {
            throw new IllegalArgumentException("The data can't be read -> Not enough data in the buffer");
        }
        byte[] buff = new byte[n];
        buffer.get(buff);
        this.data = new String(buff);
    }

    /**
     * Constructor with the data.
     *
     * @param data The data. Must not be null
     */
    protected AbstractInfo(String data) {
        Util.checkNotNull("data", data);
        if(data.length() > DATA_MAX_SIZE) {
            throw new IllegalArgumentException("The string is too long -> length = " + data.length());
        }
        this.data = data;
    }

    /**
     * Return the real size of the info
     * @return the real size of the info
     */
    public int getSize() {
        return N_SIZE + this.getN();
    }

    /**
     * Writes the data in the given buffer.
     * @param buffer The buffer in which data are written.
     */
    public void writeData(ByteBuffer buffer) {
        buffer.put(this.getN());
        buffer.put(this.data.getBytes());
    }

    /**
     * Return the data.
     * @return the data.
     */
    String getData() {
        return this.data;
    }

    /**
     * Set a new data.
     */
    void setData(String data) {
        if(data != null && data.length() < DATA_MAX_SIZE) {
            this.data = data;
        }
    }

    /**
     * Return the data length.
     * @return the data length.
     */
    public byte getN() {
        return (byte) this.data.length();
    }

    @Override
    public String toString() {
        return "N=" + this.getN() + " | ";
    }
}