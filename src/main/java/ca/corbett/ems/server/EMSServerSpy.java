package ca.corbett.ems.server;

/**
 * Mostly for debugging purposes, this interface can be used to spy on a running EMSServer,
 * assuming it's running in the same JRE, and allows you to see all traffic coming in and
 * going out.
 *
 * @author scorbo2
 * @since 2023-11-18
 */
public interface EMSServerSpy {

    /**
     * Sent when the EMSServer receives any message from any client.
     *
     * @param server     The EMSServer that received the message.
     * @param clientId   The id of the client that sent the message.
     * @param rawMessage The message.
     */
    public void messageReceived(EMSServer server, String clientId, String rawMessage);

    /**
     * Sent whenever the server sends something to any client. Typically this will be
     * a response to something that the client sent, but not always (channel subscriptions).
     *
     * @param server     The EMSServer that is sending the message.
     * @param clientId   The id of the client that will receive the message.
     * @param rawMessage The message.
     */
    public void messageSent(EMSServer server, String clientId, String rawMessage);

}
