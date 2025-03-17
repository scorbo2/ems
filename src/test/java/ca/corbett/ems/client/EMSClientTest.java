package ca.corbett.ems.client;

import ca.corbett.ems.handlers.EchoHandler;
import ca.corbett.ems.handlers.HelpHandler;
import ca.corbett.ems.handlers.WhoHandler;
import ca.corbett.ems.server.EMSServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for EMSClient
 *
 * @author scorbo2
 */
public class EMSClientTest {

    private static final int TEST_PORT = 1848;
    private static EMSServer emsServer;
    private EMSClient emsClient;

    public EMSClientTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        emsServer = new EMSServer(TEST_PORT);
        emsServer.startServer();
        Thread.sleep(150); // give it a chance to start up.
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        emsServer.stopServer();
        Thread.sleep(50); // give it a chance to stop;
    }

    @BeforeEach
    public void setUp() throws Exception {
        emsServer.removeAllCommandHandlers(); // remove the built-in handlers so we can test add/remove
        emsClient = new EMSClient();
        if (!emsClient.connect("127.0.0.1", TEST_PORT)) {
            throw new Exception("Unable to connect to test server!");
        }
    }

    @AfterEach
    public void tearDown() {
        emsClient.disconnect();
    }

    @Test
    public void testIsConnected() {
        assertTrue(emsClient.isConnected());
    }

    @Test
    public void testConnect() {
        emsClient.disconnect();
        assertFalse(emsClient.isConnected());
        emsClient.connect("127.0.0.1", TEST_PORT);
        assertTrue(emsClient.isConnected());
    }

    @Test
    public void testSendCommand() {
        EMSServerResponse response = emsClient.sendCommand("HELLO");
        assertTrue(response.isError());
        String expectedMsg = EMSServer.UNRECOGNIZED_COMMAND.replace(EMSServer.ERROR_HEADER, "");
        assertEquals(expectedMsg, response.getMessage());
    }

    @Test
    public void testSendEchoCommand() {
        emsServer.registerCommandHandler(new EchoHandler());
        EMSServerResponse response = emsClient.sendCommand("ECHO", "blah", "blooh");
        assertTrue(response.isSuccess());
        assertEquals("blah" + EMSServer.DELIMITER + "blooh", response.getMessage());
    }

    @Test
    public void testSendHelpCommand() {
        emsServer.registerCommandHandler(new HelpHandler());
        EMSServerResponse response = emsClient.sendCommand("HELP");
        assertTrue(response.isSuccess());
        assertEquals("HELP (alias ?) - Lists available commands, or shows detailed help for a specific command.", response.getMessage());
    }

    @Test
    public void testSendWhoCommand() {
        emsServer.registerCommandHandler(new WhoHandler());
        EMSServerResponse response = emsClient.sendCommand("WHO");
        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().startsWith(EMSServer.CLIENT_ID_PREFIX));
    }

//  @Test
//  public void testSubscriber_todoRemoveMe() {
//    EMSSubscriber subscriber = new EMSSubscriber();
//    subscriber.connect("127.0.0.1", 1975, "test");
//  }
}
