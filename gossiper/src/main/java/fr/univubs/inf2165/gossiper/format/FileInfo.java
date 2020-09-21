package fr.univubs.inf2165.gossiper.format;

import java.nio.ByteBuffer;

/**
 * This class represents file information in the exchanged messages.
 * It contains file name. @link{AbstractInfo}
 *
 * @author Aliyou Sylla
 * @version 1.0.0
 */
public class FileInfo extends AbstractInfo {

    /**
     * Constructor with a buffer containing file information.
     * @param buffer The buffer from which data are read. Must not be null.
     */
    public FileInfo(ByteBuffer buffer) {
        super(buffer);
    }

    /**
     * Constructor with the filename.
     *
     * @param filename The file name. Must not be null
     */
    public FileInfo(String filename) {
        super(filename);
    }

    /**
     * Return the file name
     * @return the file name
     */
    public String getFilename() {
        return super.getData();
    }

    /**
     * Set a new file name
     * @param filename The new file name
     */
    public void setFilename(String filename) {
        super.setData(filename);
    }

    @Override
    public String toString() {
        return super.toString() + "filename=" + this.getFilename();
    }
}