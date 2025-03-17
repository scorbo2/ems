package ca.corbett.ems.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for EMSServerResponse
 *
 * @author scorbo2
 */
public class EMSServerResponseTest {

    public EMSServerResponseTest() {
    }

    @Test
    public void testGetErrorMessage_SimpleCase() {
        EMSServerResponse err = new EMSServerResponse("", "ERR:Something went wrong");
        assertTrue(err.isError());
        assertFalse(err.isSuccess());
        assertEquals("Something went wrong", err.getMessage());
    }

    @Test
    public void testGetErrorMessage_LeadingWhitespace() {
        EMSServerResponse err = new EMSServerResponse("", "     \n\n\n   ERR:Something went wrong");
        assertTrue(err.isError());
        assertFalse(err.isSuccess());
        assertEquals("Something went wrong", err.getMessage());
    }

    @Test
    public void testGetErrorMessage_TrailingWhitespace() {
        EMSServerResponse err = new EMSServerResponse("", "ERR:Something went wrong  \n\n\n\n\n     ");
        assertTrue(err.isError());
        assertFalse(err.isSuccess());
        assertEquals("Something went wrong", err.getMessage());
    }

    @Test
    public void testGetErrorMessage_WhitespaceEverywhere() {
        EMSServerResponse err = new EMSServerResponse("", "  \n ERR:  \n\n\n Something went wrong   \n\n\n  ");
        assertTrue(err.isError());
        assertFalse(err.isSuccess());
        assertEquals("Something went wrong", err.getMessage());
    }

    @Test
    public void testGetErrorMessage_WonkyCaseNoMessage() {
        EMSServerResponse err = new EMSServerResponse("", "ERR:");
        assertTrue(err.isError());
        assertFalse(err.isSuccess());
        assertEquals("", err.getMessage());
    }

    @Test
    public void testGetSuccessMessage_SimpleCases() {
        EMSServerResponse ok = new EMSServerResponse("", "OK: We're all good");
        assertTrue(ok.isSuccess());
        assertFalse(ok.isError());
        assertEquals("We're all good", ok.getMessage());

        ok = new EMSServerResponse("", "OK");
        assertTrue(ok.isSuccess());
        assertFalse(ok.isError());
        assertEquals("", ok.getMessage());
    }

    @Test
    public void testGetSuccessMessage_MultiLineCase() {
        EMSServerResponse ok = new EMSServerResponse("", "Line1\nLine2\nLine3\nOK");
        assertTrue(ok.isSuccess());
        assertFalse(ok.isError());
        assertEquals("Line1\nLine2\nLine3", ok.getMessage());
    }

}
