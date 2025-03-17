package ca.corbett.ems.handlers;

import ca.corbett.ems.server.EMSServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for AbstractCommandHandler
 *
 * @author scorbo2
 */
public class AbstractCommandHandlerTest {

    private AbstractCommandHandlerImpl handler;

    public AbstractCommandHandlerTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        handler = new AbstractCommandHandlerImpl();
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getParts method, of class AbstractCommandHandler.
     */
    @Test
    public void testGetParts() {
        assertEquals(3, handler.getParts("this:that:the other").length);
        assertEquals(4, handler.getParts("this:that::the other").length);
        assertArrayEquals(null, handler.getParts(null));
    }

    @Test
    public void testCreateErrorResponse() {
        assertEquals("ERR:Something", handler.createErrorResponse("Something"));
    }

    @Test
    public void testCreateOkResponse() {
        assertEquals("OK", handler.createOkResponse());
        assertEquals("OK:Something", handler.createOkResponse("Something"));
    }

    @Test
    public void testGetAllFieldsToEndOfLine() {
        assertEquals("", handler.getAllFieldsToEndOfLine("Something", 7, ""));
        assertEquals("hello there", handler.getAllFieldsToEndOfLine("Something:hello:there", 1, " "));
    }

    public static class AbstractCommandHandlerImpl extends AbstractCommandHandler {

        public AbstractCommandHandlerImpl() {
            super("test");
        }

        @Override
        public String handle(EMSServer server, String clientId, String commandLine) {
            return "";
        }

        @Override
        public int getMinParameterCount() {
            return 0;
        }

        @Override
        public int getMaxParameterCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public String getHelpText() {
            return "Not really a command.";
        }

        @Override
        public String getUsageText() {
            return "blah";
        }
    }
}
