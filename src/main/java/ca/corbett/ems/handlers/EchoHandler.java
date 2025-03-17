package ca.corbett.ems.handlers;

import ca.corbett.ems.server.EMSServer;

import static ca.corbett.ems.server.EMSServer.DELIMITER;

/**
 * A very simple example CommandHandler that just echoes whatever it receives.
 *
 * @author scorbo2
 * @since 2024-12-30
 */
public class EchoHandler extends AbstractCommandHandler {

    public EchoHandler() {
        super("ECHO");
    }

    public EchoHandler(String name) {
        super(name);
    }

    public EchoHandler(String name, String alias) {
        super(name, alias);
    }

    @Override
    public String handle(EMSServer server, String clientId, String commandLine) {
        return createOkResponse(this.getAllFieldsToEndOfLine(commandLine, 1, ":"));
    }

    @Override
    public int getMinParameterCount() {
        return 1;
    }

    @Override
    public int getMaxParameterCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String getHelpText() {
        return "Simply echoes whatever parameters you supply.";
    }

    @Override
    public String getUsageText() {
        return name + DELIMITER + "<message>";
    }

}
