package ca.corbett.ems.server;

import ca.corbett.ems.handlers.EchoHandler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for EMSServer
 *
 * @author scorbo2
 */
public class EMSServerTest {

    private static final int TEST_PORT = 1888;

    private EMSServer server;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private String msg1;
    private String msg2;

    public EMSServerTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() throws Exception {
        server = new EMSServer(TEST_PORT);
        server.removeAllCommandHandlers(); // remove the built-in ones so we can test add/remove
        server.startServer();
        Thread.sleep(50); // give it a chance to start up
        clientSocket = new Socket("127.0.0.1", TEST_PORT);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    @AfterEach
    public void tearDown() throws Exception {
        server.stopServer();
        Thread.sleep(50); // give it a chance to stop
    }

    @Test
    public void testConnection() throws Exception {
        System.out.println("Executing testConnection()");
        server.registerCommandHandler(new EchoHandler());
        out.println("ECHO:Hello there");
        String reply = in.readLine();
        assertEquals(EMSServer.OK_HEADER + "Hello there", reply);
    }

    @Test
    public void testStopServer() throws Exception {
        System.out.println("Executing testStopServer()");
        server.registerCommandHandler(new EchoHandler());
        clientSocket = new Socket("127.0.0.1", TEST_PORT);
        server.stopServer();
        Thread.sleep(50); // give it a chance to die
        try {
            // Oddly, the socket will still report that it's open and connected.
            // The only way you can know that it's down is if readLine returns null... wtf
            //
            //assertTrue(clientSocket.isClosed());
            //assertTrue(clientSocket.isInputShutdown());
            //assertTrue(!clientSocket.isConnected());
            out.println("ECHO:Shouldn't be allowed");
            assertEquals(EMSServer.DISCONNECTED, in.readLine());
        }
        catch (Exception ignored) {
            // ignored, expected
        }
        server = new EMSServer(TEST_PORT);
        server.registerCommandHandler(new EchoHandler());
        server.startServer();
        Thread.sleep(50);
        clientSocket = new Socket("127.0.0.1", TEST_PORT);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out.println("ECHO:Restarted!");
        String reply = in.readLine();
        assertEquals(EMSServer.OK_HEADER + "Restarted!", reply);
        clientSocket.close();
    }

    @Test
    public void testGetCommands() {
        server.registerCommandHandler(new EchoHandler("TEST1"));
        server.registerCommandHandler(new EchoHandler("TEST2"));
        server.registerCommandHandler(new EchoHandler("TEST3"));
        server.registerCommandHandler(new EchoHandler("TEST4"));
        assertEquals(4, server.listCommands().size());

        // adding one of the same name shouldn't add to the list:
        server.registerCommandHandler(new EchoHandler("TEST1"));
        assertEquals(4, server.listCommands().size());
    }

    @Test
    public void testRegister() {
        server.registerCommandHandler(new EchoHandler("echo"));
        List<String> commands = server.listCommands();
        assertEquals(1, commands.size());
        assertEquals("ECHO", commands.get(0));
    }

    @Test
    public void testRegisterWithAlias() {
        server.registerCommandHandler(new EchoHandler("echo", "blah"));
        List<String> commands = server.listCommands();
        assertEquals(1, commands.size());
        assertEquals("ECHO (alias BLAH)", commands.get(0));
    }

    @Test
    public void testRemove() {
        server.registerCommandHandler(new EchoHandler("Test1"));
        server.registerCommandHandler(new EchoHandler("Test2"));
        assertEquals(2, server.listCommands().size());
        server.removeCommandHandler("Test2");
        assertEquals(1, server.listCommands().size());
        assertEquals("TEST1", server.listCommands().get(0));
        server.removeCommandHandler("Test1");
        assertEquals(0, server.listCommands().size());
    }

    @Test
    public void testExecuteCommand() {
        server.registerCommandHandler(new EchoHandler("ECHO"));
        String result = server.executeCommand("SomeClient", "ECHO:Something");
        assertEquals(EMSServer.OK_HEADER + "Something", result);
    }

    @Test
    public void testGetCommandName() {
        assertEquals("BLAH", server.getCommandName("Blah:blah"));
        assertEquals("HI", server.getCommandName("           hi"));
    }

    @Test
    public void testServerSpy() throws Exception {
        EMSServerSpy spy = new EMSServerSpy() {
            @Override
            public void messageReceived(EMSServer server, String clientId, String rawMessage) {
                msg1 = rawMessage;
            }

            @Override
            public void messageSent(EMSServer server, String clientId, String rawMessage) {
                msg2 = rawMessage;
            }

        };
        server.addServerSpy(spy);
        out.println("This is a test");
        String reply = in.readLine();
        assertNotNull(reply);
        assertNotNull(msg1);
        assertNotNull(msg2);
        assertEquals("This is a test", msg1);
        assertEquals("ERR:Unknown command", msg2);

        server.removeServerSpy(spy);
        msg1 = null;
        msg2 = null;
        out.println("Another test");
        reply = in.readLine();
        assertNotNull(reply);
        assertNull(msg1); // should no longer be spying
        assertNull(msg2);
    }
}
