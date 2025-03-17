package ca.corbett.ems.client;

import ca.corbett.ems.server.EMSServer;

/**
 * Wraps a response received from an EMSServer in response to a command - this response
 * may represent a success or failure. You can use isSuccess() and isError() to make
 * this determination, and getMessage() to receive the message accompanying the response,
 * if the response specified a message.
 * <p>
 * You can extend this class to represent more than just a raw string. For example,
 * if you have a command handler that returns json, you could parse it into a model
 * object and have your derived response handler class contain that model object.
 * </p>
 *
 * @author scorbo2
 * @since 2023-11-18
 */
public class EMSServerResponse {

    protected final String originalCommand;
    protected final String rawResponse;
    protected final String message;
    protected boolean isError;
    protected boolean isSuccess;

    /**
     * Invoked from EMSClient - you shouldn't need to ever instantiate this class manually.
     * You may want to extend this class if you have extended EMSClient to provide
     * some customization around the responses coming back.
     *
     * @param originalCommand The original command line that was sent to the server.
     * @param rawResponse     The raw response received from the server.
     */
    public EMSServerResponse(String originalCommand, String rawResponse) {
        this.originalCommand = originalCommand == null ? "" : originalCommand;
        this.rawResponse = rawResponse == null ? "" : rawResponse.trim();
        this.message = parseResponse();
    }

    /**
     * Returns the original command line that was sent to the server, complete with
     * whatever parameters were specified.
     *
     * @return The command line that triggered this response.
     */
    public String getOriginalCommand() {
        return originalCommand;
    }

    /**
     * Reports whether this response represents an error returned from the server.
     * This is logically equivalent to !isSuccess();
     *
     * @return true if the response is an error.
     */
    public boolean isError() {
        return isError;
    }

    /**
     * Reports whether this response represents a successful return from the server.
     * This is logically equivalent to !isError();
     *
     * @return true if this response was successful.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * In the case where isError() is true, this will further let you know if the
     * error was the special case of ERR:Disconnected.
     *
     * @return true if the error was a server disconnect.
     */
    public boolean isServerDisconnectError() {
        return EMSServer.DISCONNECTED.equals(rawResponse);
    }

    /**
     * Reports whether this response includes a message.
     *
     * @return True if a message was included with the response.
     */
    public boolean hasMessage() {
        return !message.isEmpty();
    }

    /**
     * Returns the parsed message (that is, not including the OK token itself).
     * This string may be empty if the OK response didn't specify a message.
     *
     * @return The message received from the server, or empty string if none.
     */
    public String getMessage() {
        return message;
    }

    /**
     * If you override this method, it's a good idea to invoke super.parseResponse()
     * up front, and then do whatever additional parsing you need to do on top of that.
     *
     * @return The raw String message returned from the server (may be multi-line).
     */
    protected String parseResponse() {
        // If the raw response is completely empty, something might be wrong:
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            isError = true;
            return "Unexpected empty response from server.";
        }

        // We're expecting either ERROR or OK somewhere in the response:
        if (!rawResponse.contains(EMSServer.ERROR_HEADER)
                && !rawResponse.contains(EMSServer.RESPONSE_OK)) {
            return "";
        }

        if (rawResponse.contains(EMSServer.ERROR_HEADER)) {
            isError = true;
            isSuccess = false;

            // Trim leading whitespace:
            String msg = rawResponse.substring(rawResponse.indexOf(EMSServer.ERROR_HEADER));

            // Remove the error header:
            if (msg.length() <= EMSServer.ERROR_HEADER.length()) {
                return "";
            }
            msg = msg.substring(EMSServer.ERROR_HEADER.length());

            // Trim trailing whitespace:
            msg = msg.replaceAll("\\n", "").trim();
            return msg;
        } else {
            isError = false;
            isSuccess = true;

            // There are three scenarios:
            //   1) a one liner starting with "OK:" followed by a message
            //   2) a one liner with just the word "OK" and nothing else
            //   3) a multi line response ending with "OK" on a line by itself
            //
            // Scenario 1:
            //   Return everything after "OK:"
            if (rawResponse.startsWith(EMSServer.OK_HEADER)) {
                return rawResponse.substring(rawResponse.indexOf(EMSServer.OK_HEADER) + EMSServer.OK_HEADER.length()).trim();
            }

            // Scenario 2:
            //   There's no message to return. This is okay. Some commands just acknowledge without returning data.
            else if (rawResponse.equals(EMSServer.RESPONSE_OK)) {
                return "";
            }

            // Scenario 3:
            // If the response was multi-line and ended with "\nOK", strip off the last line.
            // This makes it mildly easier for client code to process a multi-line response.
            if (rawResponse.endsWith("\n" + EMSServer.RESPONSE_OK)) {
                return rawResponse.substring(0, rawResponse.length() - 3).trim();
            }

            // If we get here, the message is in an unexpected format, so just return it as-is
            // and hope the client knows how to parse it out:
            return rawResponse;
        }
    }

}
