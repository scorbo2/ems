package ca.corbett.ems.handlers.channel;

import ca.corbett.ems.server.ChannelManager;
import ca.corbett.ems.handlers.AbstractCommandHandler;
import ca.corbett.ems.server.EMSServer;

import static ca.corbett.ems.server.EMSServer.DELIMITER;

/**
 * Unsubscribes from the named channel. The client issuing this command will no longer
 * receive messages destined for that channel.
 * <p>
 *     It is not an error condition if the named channel does not exist or
 *     if the client wasn't previously subscribed to that channel. In any
 *     case, this command will return an ok response.
 * </p>
 *
 * @author scorbo2
 * @since 2023-11-24
 */
public class UnsubscribeHandler extends AbstractCommandHandler {

    public UnsubscribeHandler() {
        super("UNSUB", "UNSUBSCRIBE");
    }

    @Override
    public int getMinParameterCount() {
        return 1;
    }

    @Override
    public int getMaxParameterCount() {
        return 1;
    }

    @Override
    public String getHelpText() {
        return "Stops listening for messages on the given channel.";
    }

    @Override
    public String handle(EMSServer server, String clientId, String commandLine) {
        String[] parts = getParts(commandLine);
        if (parts.length != 2) {
            return createErrorResponse("Expected 1 parameter (channel name)");
        }
        ChannelManager.getInstance().unsubscribeFromChannel(clientId, parts[1]);
        return createOkResponse();
    }

    @Override
    public String getUsageText() {
        return name + DELIMITER + "<channel>";
    }

}
