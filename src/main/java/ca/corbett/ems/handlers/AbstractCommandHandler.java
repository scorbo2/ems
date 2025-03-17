package ca.corbett.ems.handlers;

import ca.corbett.ems.server.EMSServer;

import java.util.Objects;

/**
 * Provides an extension point your application can use to implement custom commands
 * for an EMSServer. For an example implementation, see the handlers in this package.
 * The most important method here is handle(), which does the actual work
 * that this command is responsible for.
 *
 * @author scorbo2
 * @since 2024-12-30
 */
public abstract class AbstractCommandHandler {

    protected final String name;
    protected final String alias;

    /**
     * You must specify a command name when creating a command. Command names are
     * case-insensitive, and any leading or trailing whitespace is removed.
     * Your command name does not necessarily have to be unique. Commands with the same
     * name are registered in a "last one wins" way, so even built-in commands can be
     * replaced on the fly with new ones of the same name.
     *
     * @param name The name of this command.
     */
    public AbstractCommandHandler(String name) {
        this(name, null);
    }

    /**
     * You can optionally specify an alias for this command when creating it - the command
     * can then be invoked either by its name or by its alias. Both names and aliases are
     * case-insensitive, and any leading or trailing whitespace is removed.
     *
     * @param name  The name of this command.
     * @param alias An optional alias for this command (can be null or empty).
     */
    public AbstractCommandHandler(String name, String alias) {
        this.name = (name == null || name.trim().isEmpty()) ? "UNNAMED_COMMAND" : name.trim().toUpperCase();
        this.alias = (alias == null || alias.trim().isEmpty()) ? null : alias.trim().toUpperCase();
    }

    /**
     * Returns the minimum number of parameters expected by this command. This can be used
     * by applications to validate a command before it is sent, but the default implementation
     * provided by EMS ignores this parameter.
     *
     * @return The minimum number of parameters expected by this command. Can be 0.
     */
    public abstract int getMinParameterCount();

    /**
     * Returns an optional maximum number of parameters expected by this command (a value of
     * Integer.MAX_VALUE is considered to mean no upper limit). This can be used
     * by applications to validate a command before it is sent, but the default implementation
     * provided by EMS ignores this parameter.
     *
     * @return The max number of parameters expected by this command, or Integer.MAX_VALUE (no limit).
     */
    public abstract int getMaxParameterCount();

    /**
     * Returns a very short (one line) usage help text for this command, describing the command name
     * and any expected parameters.
     *
     * @return Short usage example.
     */
    public abstract String getUsageText();

    /**
     * Returns a short (one line) description of what this command does.
     *
     * @return A short text description of this command.
     */
    public abstract String getHelpText();

    /**
     * Invoked when this command is executed, this method is responsible for doing the
     * actual work of the command. Commands are expected to return some String response,
     * which can either be a single line or multi-line. Use the createOkResponse() and
     * createErrorResponse() helper methods as needed to package up the response.
     *
     * @param server      The EMSServer that received the command.
     * @param clientId    The unique id of the client who executed the command.
     * @param commandLine The exact command line with all parameters that were specified.
     * @return The String response to this command.
     */
    public abstract String handle(EMSServer server, String clientId, String commandLine);

    /**
     * Returns the name of this command.
     *
     * @return The command name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the optional alias of this command, if there is one.
     *
     * @return The command alias, if specified, or null if there is no alias.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Invoke this to split the given command line on the delimiter specified by
     * DELIMITER within EMSServer. This is shorthand for commandLine.split(EMSServer.DELIMITER)
     *
     * @param commandLine The raw command line
     * @return An array of all fields contained within the command line.
     */
    protected String[] getParts(String commandLine) {
        if (commandLine == null) {
            return null;
        }
        return commandLine.split(EMSServer.DELIMITER);
    }

    /**
     * Use this to package up an error response. This is shorthand for
     * EMSServer.ERROR_HEADER + errMsg.replaceAll("\\n","")
     * <p>
     * Note: error messages are one-liners in EMS.
     * If the given message contains newlines, they are removed.
     *
     * @param errMsg The error message to return. Newlines are removed.
     * @return A formatted error response string.
     */
    protected String createErrorResponse(String errMsg) {
        String msg = errMsg == null ? "" : errMsg;
        return EMSServer.ERROR_HEADER + msg.replaceAll("\\n", "");
    }

    /**
     * Creates a generic empty OK response without a message.
     *
     * @return "OK"
     */
    protected String createOkResponse() {
        return createOkResponse(null);
    }

    /**
     * Creates an OK response suitable for the given okMsg - if the given
     * message contains newlines, a multi-line response is generated, otherwise
     * a single-line response is generated.
     *
     * @param okMsg The message to include with the ok response, or null for no message.
     * @return A formatted okay response string.
     */
    protected String createOkResponse(String okMsg) {
        if (okMsg == null || okMsg.trim().isEmpty()) {
            return EMSServer.RESPONSE_OK;
        }
        okMsg = okMsg.trim();
        if (okMsg.contains("\n")) {
            if (!okMsg.endsWith("\n")) {
                okMsg += "\n";
            }
            okMsg += EMSServer.RESPONSE_OK;
        } else {
            okMsg = EMSServer.OK_HEADER + okMsg;
        }
        return okMsg;
    }

    /**
     * Provides a handy way to return all fields starting from a given index right to the end
     * of the command line as a single string, joined together with the given joiner.
     * For example, given the following command line:
     * <blockquote>SOME_COMMAND:field1:is:one:long:string</blockquote>
     * You can invoke getAllFieldsToEndOfLine(cmdLine, 2, " ") to retrieve the following string:
     * <blockquote>field1 is one long string</blockquote>
     * The returned string may be empty if your given index is out of bounds.
     *
     * @param commandLine        The raw command line
     * @param startingFieldIndex The index of the field in question.
     * @param joiner             The string to use to join different fields together.
     * @return The joined string combining together the specified fields.
     */
    protected String getAllFieldsToEndOfLine(String commandLine, int startingFieldIndex, String joiner) {
        if (commandLine == null || startingFieldIndex < 0) {
            return null;
        }
        String[] parts = getParts(commandLine);
        if (parts.length < startingFieldIndex) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = startingFieldIndex; i < parts.length; i++) {
            sb.append(parts[i]);
            if (i < parts.length - 1) {
                sb.append(joiner);
            }
        }
        return sb.toString();
    }

    /**
     * Returns the name of this command.
     *
     * @return The command name.
     */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.name);
        hash = 47 * hash + Objects.hashCode(this.alias);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractCommandHandler other = (AbstractCommandHandler) obj;
        String myName = name == null ? "null" : name;
        String myAlias = alias == null ? "null" : alias;
        String otherName = other.name == null ? "null" : other.name;
        String otherAlias = other.alias == null ? "null" : other.alias;
        return (myName.equals(otherName) && myAlias.equals(otherAlias));
    }

}
