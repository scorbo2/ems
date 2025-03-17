package ca.corbett.ems.server;

import ca.corbett.ems.handlers.AbstractCommandHandler;
import ca.corbett.ems.handlers.EchoHandler;
import ca.corbett.ems.handlers.HelpHandler;
import ca.corbett.ems.handlers.WhoHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The EMSServer class provides a very easy way to expose an API to remote clients.
 * By default, a running instance of EMSServer only has a small number of built-in
 * commands, but by invoking registerCommandHandler() with your custom implementation
 * of AbstractCommandHandler, you can add whatever custom commands your application
 * wants to expose to remote clients. You generally shouldn't need to extend this
 * class even in highly customized environments. For an example use of EMSServer
 * with custom command handlers, take a look at the <b>ems-example-app</b> standalone project,
 * which provides a very lightweight kafka-like server that can be used to subscribe
 * to named channels and listen for activity on those channels. The possibilities for
 * embedding EMSServer into an application are there limited only by whatever you can
 * stick into a custom command handler.
 * <p>
 * <B>Spying on an EMSServer</B><br>
 * Mostly for debugging purposes, there is an EMSServerSpy interface that can be used
 * with the addServerSpy() and removeServerSpy() methods in this class to receive
 * a notification when any message comes in from any client or goes out to any client.
 * </p>
 *
 * @author scorbo2
 * @since 2024-12-30
 */
public class EMSServer {

    private static final Logger logger = Logger.getLogger(EMSServer.class.getName());

    public static final String DELIMITER = ":";
    public static final String RESPONSE_OK = "OK";
    public static final String RESPONSE_ERR = "ERR";
    public static final String OK_HEADER = RESPONSE_OK + DELIMITER;
    public static final String ERROR_HEADER = RESPONSE_ERR + DELIMITER;
    public static final String UNRECOGNIZED_COMMAND = ERROR_HEADER + "Unknown command";
    public static final String DISCONNECTED = ERROR_HEADER + "Disconnected";
    public static final String CLIENT_ID_PREFIX = "EMSC"; // Easy Messaging Service Client

    protected final List<EMSServerSpy> serverSpies;
    protected final Set<AbstractCommandHandler> commandHandlers;
    protected IOException startupException;
    private final String hostname;
    private final int listeningPort;
    private EMSServerThread serverThread;

    /**
     * Creates a new instance of EMSServer configured to listen on the given server port,
     * but does not actually start up. Use startServer() to bind to the given port
     * and start listening for connections.
     *
     * @param port A TCP port larger than 1024 (unless you're running as root I guess).
     */
    public EMSServer(int port) {
        this("localhost", port);
    }

    /**
     * Creates a new instance of EMSServer configured to listen on the given
     * host and port, but does not actually start up. Use startServer() to bind to
     * the given address and port and start listening for connections.
     *
     * @param host Either "localhost" or some local IP or any other hostname that can be resolved.
     * @param port A TCP port larger than 1024.
     */
    public EMSServer(String host, int port) {
        serverSpies = new ArrayList<>();
        commandHandlers = new HashSet<>();
        commandHandlers.add(new EchoHandler());
        commandHandlers.add(new HelpHandler());
        commandHandlers.add(new WhoHandler());
        hostname = host;
        listeningPort = port;
    }

    /**
     * Starts listening for connections. Use stopServer() to shut the server down.
     */
    public void startServer() {
        startupException = null;
        if (serverThread != null) {
            logger.log(Level.SEVERE, "Received start() command when server is already up; ignoring.");
            return;
        }

        logger.log(Level.INFO, "Starting EMS server on port {0}", listeningPort);
        serverThread = new EMSServerThread(this, hostname, listeningPort);
        for (EMSServerSpy spy : serverSpies) {
            serverThread.addServerSpy(spy);
        }
        serverThread.start();
    }

    /**
     * Stops the server and kills all current client connections.
     * You can call startServer() after this call to start it back up on the same port.
     */
    public void stopServer() {
        if (serverThread == null) {
            logger.log(Level.SEVERE, "Received a stop() command when server is not running; ignored");
            return;
        }

        logger.log(Level.INFO, "Shutting down server thread...");
        serverThread.interrupt();
        serverThread = null;
    }

    /**
     * Reports whether the server is up and running.
     *
     * @return true if the server is up and running.
     */
    public boolean isUp() {
        return serverThread != null && serverThread.isRunning();
    }

    /**
     * Can be used by command handlers to send a message directly to a specific client.
     * Does nothing if the given client doesn't exist.
     *
     * @param clientId The unique id of the client in question.
     * @param msg      The raw message to send to that client.
     */
    public void sendToClient(String clientId, String msg) {
        if (serverThread != null) {
            serverThread.sendToClient(clientId, msg);
        }
    }

    /**
     * Returns a list of command names. The returned list is sorted alphabetically, and includes
     * the aliases of any command handlers that define an alias.
     *
     * @return A sorted list of commands, by name.
     */
    public List<String> listCommands() {
        List<String> commands = new ArrayList<>();
        for (AbstractCommandHandler handler : commandHandlers) {
            String command = handler.getName();
            if (handler.getAlias() != null) {
                command += " (alias " + handler.getAlias() + ")";
            }
            commands.add(command);
        }
        commands.sort(null);
        return commands;
    }

    /**
     * Registers the given command handler with this EMSServer. If the name of the given
     * command is not unique, this command effectively replaces the previous one of the
     * same name.
     *
     * @param handler An implementation class of AbstractCommandHandler.
     */
    public void registerCommandHandler(AbstractCommandHandler handler) {
        if (handler == null) {
            return;
        }

        // Nuke any previous command by this name or alias:
        AbstractCommandHandler existingHandler = getCommandHandler(handler.getName());
        if (existingHandler != null) {
            removeCommandHandler(handler.getName());
        }
        if (handler.getAlias() != null) {
            existingHandler = getCommandHandler(handler.getAlias());
            if (existingHandler != null) {
                removeCommandHandler(handler.getAlias());
            }
        }

        // Now this one is in play:
        commandHandlers.add(handler);
    }

    /**
     * Unregisters the command handler by the given name, if there is one.
     * You can supply the command's name or its alias, case insensitive.
     *
     * @param command The name or alias of the command to be removed.
     */
    public void removeCommandHandler(String command) {
        if (command == null) {
            return;
        }
        command = command.trim().toUpperCase();
        for (AbstractCommandHandler handler : commandHandlers) {
            if (command.equals(handler.getName()) || command.equals(handler.getAlias())) {
                commandHandlers.remove(handler);
                return;
            }
        }
    }

    /**
     * Unregisters all command handlers - this effectively renders the EMSServer useless,
     * at least until new command handlers are registered.
     */
    public void removeAllCommandHandlers() {
        commandHandlers.clear();
    }

    /**
     * Returns the command handler for the given command, if any. This is useful to interrogate
     * the command for usage or help info.
     *
     * @param name The name or alias of the command in question, case insensitive.
     * @return The command handler instance, or null if not found.
     */
    public AbstractCommandHandler getCommandHandler(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        name = name.trim().toUpperCase();
        for (AbstractCommandHandler handler : commandHandlers) {
            if (name.equals(handler.getName()) || name.equals(handler.getAlias())) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Executes the given command line by looking up and invoking its associated
     * command handler. Returns the output of the command in String form.
     *
     * @param clientId    The identifier of the client executing the command.
     * @param commandLine The command and any parameters.
     * @return The output of the command, or UNRECOGNIZED_COMMAND if not found.
     */
    public String executeCommand(String clientId, String commandLine) {
        AbstractCommandHandler handler = getCommandHandler(getCommandName(commandLine));
        return (handler == null) ? UNRECOGNIZED_COMMAND : handler.handle(this, clientId, commandLine);
    }

    /**
     * Adds an EMSServerSpy which will receive notification when any message comes in from
     * any client, and when any response goes out to any client.
     *
     * @param spy An EMSServerSpy instance.
     */
    public void addServerSpy(EMSServerSpy spy) {
        serverSpies.add(spy);
        if (serverThread != null) {
            serverThread.addServerSpy(spy);
        }
    }

    /**
     * Removes the specified EMSServerSpy from our spy list if it is present.
     *
     * @param spy The EMSServerSpy to be removed.
     */
    public void removeServerSpy(EMSServerSpy spy) {
        serverSpies.remove(spy);
        if (serverThread != null) {
            serverThread.removeServerSpy(spy);
        }
    }

    /**
     * If the server failed to start, this method will return the exception that was
     * triggered, if any, to cause the failure.
     *
     * @return An IOException, or null if there was no exception on startup.
     */
    public IOException getStartupException() {
        return startupException;
    }

    /**
     * Invoked from our server thread if something goes wrong when it's trying to start up.
     */
    void setStartupException(IOException ioe) {
        startupException = ioe;
    }

    /**
     * Invoked internally to parse the command name out of the given command line.
     *
     * @param commandLine The command with any parameters included.
     * @return Just the command name on its own.
     */
    protected String getCommandName(String commandLine) {
        if (commandLine == null) {
            return null;
        }
        String name;
        if (commandLine.contains(DELIMITER)) {
            String[] parts = commandLine.split(DELIMITER);
            name = parts[0];
        } else {
            name = commandLine;
        }

        return name.trim().toUpperCase();
    }

}
