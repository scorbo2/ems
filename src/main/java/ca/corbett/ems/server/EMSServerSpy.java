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

    /**
     * Sent whenever a new client connects to the server. This message will only be sent for clients
     * that connect AFTER the server spy is added to the server. Any client already connected before this
     * spy was added will not trigger this message.
     *
     * @param server The EMSServer that is sending the message.
     * @param clientId The id of the client that just connected.
     */
    public void clientConnected(EMSServer server, String clientId);

    /**
     * Sent whenever a client disconnects from the server. This message might not be received if the client
     * disconnects in an uncontrolled manner (for example, network failure, sudden crash on the client side, etc).
     *
     * @param server The EMSServer that is sending the message.
     * @param clientId The id of the client that just disconnected.
     */
    public void clientDisconnected(EMSServer server, String clientId);
}
