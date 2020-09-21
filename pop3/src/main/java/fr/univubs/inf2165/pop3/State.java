
package fr.univubs.inf2165.pop3;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This is an enumeration of the POP3' different states.
 *
 * Minimal POP3 Commands:
 *    USER name               valid in the AUTHORIZATION state
 *    PASS string
 *    QUIT
 *
 *    STAT                    valid in the TRANSACTION state
 *    LIST [msg]
 *    RETR msg
 *    DELE msg
 *    NOOP
 *    RSET
 *    QUIT
 *
 * @author Aliyou Sylla
 * @version 14/10/2019
 */

public enum State {

    /**
     * Once the TCP connection has been opened by a POP3 client, the POP3
     * server is now in the AUTHORIZATION state. The client must
     * now identify and authenticate itself to the POP3 server
     *
     * Once the POP3 server has determined that the client should be given
     * access to the appropriate maildrop, the POP3 server then acquires
     * an exclusive-access lock on the maildrop, as necessary to prevent
     * messages from being modified or removed before the session enters
     * the UPDATE state. If the lock is successfully acquired, the POP3
     * server responds with a positive status indicator.  The POP3 session
     * now enters the TRANSACTION state, with no messages marked as deleted.
     * If the maildrop cannot be opened for some reason (for example, a lock can
     * not be acquired, the client is denied access to the appropriate
     * maildrop, or the maildrop cannot be parsed), the POP3 server responds
     * with a negative status indicator.  (If a lock was acquired but the
     * POP3 server intends to respond with a negative status indicator, the
     * POP3 server must release the lock prior to rejecting the command.)
     * After returning a negative status indicator, the server may close the
     * connection.  If the server does not close the connection, the client
     * may either issue a new authentication command and start again, or the
     * client may issue the QUIT command.
     *
     * After the POP3 server has opened the maildrop, it assigns a message-
     * number to each message, and notes the size of each message in octets.
     * The first message in the maildrop is assigned a message-number of
     * "1", the second is assigned "2", and so on, so that the nth message
     *  in a maildrop is assigned a message-number of "n".  In POP3 commands
     *  and responses, all message-numbers and message sizes are expressed in
     *  base-10 (i.e., decimal).
     *
     *  RFC 1939                          POP3                          May 1996
     */
    AUTHORIZATION,

    /**
     * Once the client has successfully identified itself to the POP3 server
     * and the POP3 server has locked and opened the appropriate maildrop,
     * the POP3 session is now in the TRANSACTION state.  The client may now
     * issue any of the following POP3 commands repeatedly.  After each
     * command, the POP3 server issues a response.  Eventually, the client
     * issues the QUIT command and the POP3 session enters the UPDATE state.
     *
     *  RFC 1939                          POP3                          May 1996
     */
    TRANSACTION,

    /**
     * When the client issues the QUIT command from the TRANSACTION state,
     * the POP3 session enters the UPDATE state.  (Note that if the client
     * issues the QUIT command from the AUTHORIZATION state, the POP3
     * session terminates but does NOT enter the UPDATE state.)
     *
     * If a session terminates for some reason other than a client-issued
     * QUIT command, the POP3 session does NOT enter the UPDATE state and
     * MUST not remove any messages from the maildrop.
     *
     *  RFC 1939                          POP3                          May 1996
     */
    UPDATE;

    private static EnumMap<State, Set<Keyword>> stateMap = new EnumMap<>(State.class);

    /**
     * Return a set of keywords available for a given state.
     *
     * @param state State
     * @return a set of keywords available for a state.
     */
    public static Set<Keyword> getKeywords(State state) {
        if(stateMap.size() == 0) {
            stateMap.put(State.AUTHORIZATION, new HashSet<>(
                    Arrays.asList(Keyword.USER, Keyword.PASS)));
            stateMap.put(State.TRANSACTION, new HashSet<>(
                    Arrays.asList(Keyword.STAT, Keyword.LIST, Keyword.RETR, Keyword.DELE, Keyword.NOOP, Keyword.RSET)));
            stateMap.put(State.UPDATE, new HashSet<>());
        }
        return stateMap.get(state);
    }
}