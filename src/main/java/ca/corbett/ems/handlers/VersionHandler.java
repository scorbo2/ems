package ca.corbett.ems.handlers;

import ca.corbett.ems.Version;
import ca.corbett.ems.server.EMSServer;

public class VersionHandler extends AbstractCommandHandler {

    public VersionHandler() {
        super("VERSION", "VER");
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
        return createOkResponse(Version.FULL_NAME);
    }
}
