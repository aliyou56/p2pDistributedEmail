package fr.univubs.inf2165.gossiper.format;

import java.nio.ByteBuffer;

/**
 * This class represents user information in the exchanged messages.
 * It contains user name. @link{AbstractInfo}
 *
 * @author Aliyou Sylla
 * @version 1.0.0
 */
public class UserInfo extends AbstractInfo {

    /**
     * Constructor with a buffer containing the user information.
     * @param buffer The buffer from which data are read. Must not be null.
     */
    public UserInfo(ByteBuffer buffer) {
        super(buffer);
    }

    /**
     * Constructor with the username.
     *
     * @param username The user name. Must not be null
     */
    public UserInfo(String username) {
        super(username);
    }

    /**
     * Return the user name
     * @return the user name
     */
    public String getUsername() {
        return super.getData();
    }

    /**
     * Set a new user name
     * @param username the new user name
     */
    public void setUsername(String username) {
        super.setData(username);
    }

    @Override
    public String toString() {
        return super.toString() + "username=" + this.getUsername();
    }
}