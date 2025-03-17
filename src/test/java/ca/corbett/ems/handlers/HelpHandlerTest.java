package ca.corbett.ems.handlers;

import ca.corbett.ems.client.EMSServerResponse;
import ca.corbett.ems.server.EMSServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the HelpHandler, just to verify response format.
 *
 * @author scorbo2
 */
class HelpHandlerTest {

    @Test
    public void testHelpHandler_withHelpCommand_shouldReturnCommandList() {
        // GIVEN an EMS server (not actually started, but pre-populated with built-in commands):
        EMSServer server = new EMSServer(1975);

        // WHEN we execute the help command:
        String response = server.executeCommand("client1", "HELP");

        // THEN the response should be multi-line and end with OK
        assertTrue(response.contains("\n"));
        assertTrue(response.endsWith("\nOK"));
    }

}