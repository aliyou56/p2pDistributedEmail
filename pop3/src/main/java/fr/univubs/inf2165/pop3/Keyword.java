
package fr.univubs.inf2165.pop3;

/**
 * This is an enumeration of the POP3' different keywords used
 * by this implementation.
 *
 * @author Aliyou Sylla
 * @version 14/10/2019 
 */
public enum Keyword {

    /**
     * Arguments:
     *  a string identifying a mailbox (required), which is of
     *  significance ONLY to the server
     *
     * Restrictions:
     *  may only be given in the AUTHORIZATION state after the POP3
     *  greeting or after an unsuccessful USER or PASS command
     *
     * Possible Responses:
     *      +OK name is a valid mailbox
     *      -ERR never heard of mailbox name
     *
     * Examples:
     *      C: USER frated
     *      S: -ERR sorry, no mailbox for frated here
     *       ...
     *      C: USER mrose
     *      S: +OK mrose is a real hoopy frood
     */
    USER,

    /**
     * Arguments:
     *  a server/mailbox-specific password (required)
     *
     * Restrictions:
     *  may only be given in the AUTHORIZATION state immediately
     *  after a successful USER command
     *
     * Possible Responses:
     *      +OK maildrop locked and ready
     *      -ERR invalid password
     *      -ERR unable to lock maildrop
     */
    PASS,

    /**
     * Arguments: none
     *
     * Restrictions: none
     *
     * State :
     *  - AUTHORIZATION
     *      Possible Responses:
     *          +OK
     *
     *  - UPDATE 
     *      Possible Responses:
     *          +OK
     *          -ERR some deleted messages not removed

     */
    QUIT,

    /**
     * The positive response consists of "+OK" followed by a single
     * space, the number of messages in the maildrop, a single
     * space, and the size of the maildrop in octets.
     *
     * Note that messages marked as deleted are not counted in either total.
     *
     *  Possible Responses:
     *      +OK nn mm
     *
     *  Examples:
     *      C: STAT
     *      S: +OK 2 320
     */
    STAT,

    /**
     * If an argument was given and the POP3 server issues a
     * positive response with a line containing information for
     * that message.  This line is called a "scan listing" for
     * that message.
     *
     * If no argument was given and the POP3 server issues a
     * positive response, then the response given is multi-line.
     * After the initial +OK, for each message in the maildrop,
     * the POP3 server responds with a line containing
     * information for that message.  This line is also called a
     * "scan listing" for that message.  If there are no
     * messages in the maildrop, then the POP3 server responds
     * with no scan listings--it issues a positive response
     * followed by a line containing a termination octet and a CRLF pair.

     * Note that messages marked as deleted are not listed.
     *
     *  Possible Responses:
     *      +OK scan listing follows
     *      -ERR no such message
     *
     *  Examples:
     *      C: LIST
     *      S: +OK 2 messages (320 octets)
     *      S: 1 120
     *      S: 2 200
     *      S: .
     *      ...
     *      C: LIST 2
     *      S: +OK 2 200
     *      ...
     *      C: LIST 3
     *      S: -ERR no such message, only 2 messages in maildrop
     */
    LIST,

    /**
     * If the POP3 server issues a positive response, then the
     * response given is multi-line.  After the initial +OK, the
     * POP3 server sends the message corresponding to the given
     * message-number, being careful to byte-stuff the termination
     * character (as with all multi-line responses).
     *
     *  Possible Responses:
     *      +OK message follows
     *      -ERR no such message
     *
     * Examples:
     *      C: RETR 1
     *      S: +OK 120 octets
     *      S: <the POP3 server sends the entire message here>
     *      S: .
     */
    RETR,

    /**
     * The POP3 server marks the message as deleted. Any future
     * reference to the message-number associated with the message
     * in a POP3 command generates an error.  The POP3 server does
     * not actually delete the message until the POP3 session
     * enters the UPDATE state.
     *
     * Possible Responses:
     *      +OK message deleted
     *      -ERR no such message
     *
     * Examples:
     *      C: DELE 1
     *      S: +OK message 1 deleted
     *       ...
     *      C: DELE 2
     *      S: -ERR message 2 already deleted
     */
    DELE,

    /**
     * The POP3 server does nothing, it merely replies with a
     * positive response.
     */
    NOOP,

    /**
     * If any messages have been marked as deleted by the POP3server,
     * they are unmarked. The POP3 server then replies with a positive response.
     *
     * Possible Responses:
     *      +OK
     *
     * Examples:
     *      C: RSET
     *      S: +OK maildrop has 2 messages (320 octets)*
     */
    RSET;
}