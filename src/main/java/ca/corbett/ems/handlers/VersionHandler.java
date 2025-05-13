package ca.corbett.ems.handlers;

import ca.corbett.ems.Version;
import ca.corbett.ems.server.EMSServer;

/**
 * A singleton command handler to return server name and/or version.
 * By default, this will return Version.FULL_NAME, but you can alter
 * it to return whatever name/version information you want.
 *
 * @author scorbo2
 * @since 2025-05-12
 */
public class VersionHandler extends AbstractCommandHandler {

    private static VersionHandler instance;
    private static String serverName = Version.FULL_NAME;

    private VersionHandler() {
        super("VERSION", "VER");
    }

    /**
     * This class is singleton to allow dynamically changing the server
     * name - see setServerName.
     *
     * @return The single instance of this command handler.
     */
    public static VersionHandler getInstance() {
        if (instance == null) {
            instance = new VersionHandler();
        }
        return instance;
    }

    /**
     * Set an optional name/version for this server. This will be returned when clients
     * query the VERSION command. The default is the full name and version
     * of this application as specified in the Version class.
     *
     * @param name The new human-readable name/version for this server.
     */
    public void setServerName(String name) {
        serverName = (name == null || name.isBlank()) ? Version.FULL_NAME : name;
    }

    /**
     * Returns the current server name/version that will be reported by the VERSION command.
     * You can alter this with setServerName.
     *
     * @return The currently set server name/version string.
     */
    public String getServerName() {
        return serverName;
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
        return "Returns the version of EMS running this server.";
    }

    @Override
    public String handle(EMSServer server, String clientId, String commandLine) {
        return createOkResponse(serverName);
    }
}
