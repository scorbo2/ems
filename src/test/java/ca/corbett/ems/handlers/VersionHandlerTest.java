package ca.corbett.ems.handlers;

import ca.corbett.ems.Version;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionHandlerTest {

    @Test
    public void testGetServerName_withDefaults() {
        // GIVEN a default setup:
        VersionHandler handler = VersionHandler.getInstance();

        // WHEN we query for server name:
        String actualRaw = handler.getServerName();
        String actualHandled = handler.handle(null, "client", "");

        // THEN we should see the default server name/version:
        assertEquals(Version.FULL_NAME, actualRaw);
        assertEquals("OK:"+Version.FULL_NAME, actualHandled);
    }

    @Test
    public void testGetServerName_withManualServerName() {
        // GIVEN a setup with a custom server banner:
        final String expectedBanner = "My amazing server version 99.9";

        // WHEN we query for server name:
        VersionHandler.getInstance().setServerName(expectedBanner);
        String actualRaw = VersionHandler.getInstance().getServerName();
        String actualHandled = VersionHandler.getInstance().handle(null, "client", "");

        // THEN we should see the expected banner:
        assertEquals(expectedBanner, actualRaw);
        assertEquals("OK:"+expectedBanner, actualHandled);

        // test cleanup: because it's singleton, and because we can't control the order in which
        // tests are run, we should behave nicely and set it back to the default when we're done here:
        VersionHandler.getInstance().setServerName(Version.FULL_NAME);
    }
}