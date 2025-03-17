package ca.corbett.ems.handlers;

import ca.corbett.ems.server.EMSServer;

/**
 * A built-in handler that clients can use to learn their own client id on an EMSServer.
 *
 * @author scorbo2
 * @since 2023-11-18
 */
public class WhoHandler extends AbstractCommandHandler {

    public WhoHandler() {
        super("WHO", "WHOAMI");
    }

    @Override
    public int getMinParameterCount() {
        return 0;
    }

    @Override
    public int getMaxParameterCount() {
        return 0;
    }

    @Override
    public String getUsageText() {
        return name;
    }

    @Override
    public String getHelpText() {
        return "Returns the client id for this client connection.";
    }

    @Override
    public String handle(EMSServer server, String clientId, String commandLine) {
        return createOkResponse(clientId);
    }

}
