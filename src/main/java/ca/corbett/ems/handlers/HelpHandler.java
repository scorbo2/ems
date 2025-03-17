package ca.corbett.ems.handlers;

import ca.corbett.ems.server.EMSServer;

import static ca.corbett.ems.server.EMSServer.DELIMITER;
import static ca.corbett.ems.server.EMSServer.UNRECOGNIZED_COMMAND;

import java.util.List;

/**
 * A simple handler to list available commands along with their help usage.
 * The command is "help" with an alias of "?" and takes an optional parameter which
 * is the specific command for which you wish to receive help. Without any parameters,
 * this command will list all commands that are registered on this EMSServer along
 * with a brief description of each.
 *
 * @author scorbo2
 * @since 2024-12-30
 */
public class HelpHandler extends AbstractCommandHandler {

    public HelpHandler() {
        super("help", "?");
    }

    @Override
    public int getMinParameterCount() {
        return 0;
    }

    @Override
    public int getMaxParameterCount() {
        return 1;
    }

    @Override
    public String getHelpText() {
        return "Lists available commands, or shows detailed help for a specific command.";
    }

    @Override
    public String handle(EMSServer server, String clientId, String commandLine) {

        // show help for a specific command
        if (commandLine.contains(DELIMITER)) {
            String command = getAllFieldsToEndOfLine(commandLine, 1, DELIMITER);
            AbstractCommandHandler handler = server.getCommandHandler(command);
            if (handler == null) {
                return UNRECOGNIZED_COMMAND;
            }
            return handler.getHelpText() +
                    "\nUSAGE: " +
                    handler.getUsageText() +
                    "\n" +
                    createOkResponse();
        }

        // List all commands:
        else {
            StringBuilder sb = new StringBuilder();
            List<String> commands = server.listCommands();
            for (String cmd : commands) {
                AbstractCommandHandler handler = server.getCommandHandler(cmd.split(" ")[0]);
                if (handler != null) {
                    sb.append(cmd);
                    sb.append(" - ");
                    sb.append(handler.getHelpText());
                    sb.append("\n");
                }
            }
            sb.append(createOkResponse());
            return sb.toString();
        }
    }

    @Override
    public String getUsageText() {
        return name + "[" + DELIMITER + "<command>]";
    }

}
