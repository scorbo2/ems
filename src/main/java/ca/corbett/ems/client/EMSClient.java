package ca.corbett.ems.client;

import ca.corbett.ems.server.EMSServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides client access to a remote EMS server. You can either instantiate this class
 * and use it directly if you don't mind forming the commands and parsing the raw responses
 * yourself. The alternative is to extend this class and provide more application-friendly
 * wrapper methods around sendCommand() and parseResponse() so that your application code
 * can interact with the remote server in a more natural way.
 *
 * @author scorbo2
 * @since 2024-12-30
 */
public class EMSClient {

    private static final Logger logger = Logger.getLogger(EMSClient.class.getName());

    protected boolean isConnected;
    protected Socket clientSocket;
    protected PrintWriter out;
    protected BufferedReader in;

    public EMSClient() {
        isConnected = false;
    }

    /**
     * Reports whether this EMSClient has an active connection to a remote EMS server.
     *
     * @return true if currently connected.
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Attempts to connect to a remote EMS server at the given address and port.
     * No exception is thrown - the method returns true if a connection was established.
     * (Exceptions will be logged). If you invoked this method while a current connection
     * is up, that connection will be killed in favour of the new one.
     *
     * @param remoteAddress The address of the EMS Server.
     * @param remotePort    The remote port of the EMS Server.
     * @return true if a connection was successfully established.
     */
    public boolean connect(String remoteAddress, int remotePort) {
        if (isConnected) {
            disconnect();
        }
        try {
            clientSocket = new Socket(remoteAddress, remotePort);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            isConnected = true;
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Unable to connect to EMS server at " + remoteAddress + ":" + remotePort, ioe);
            clientSocket = null;
            out = null;
            in = null;
        }
        return isConnected;
    }

    /**
     * Terminates the active connection, if any. Safe to invoke multiple times.
     */
    public void disconnect() {
        if (isConnected) {
            isConnected = false;
            try {
                clientSocket.close();
                clientSocket = null;
                out = null;
                in = null;
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "EMSClient caught exception while disconnecting", ioe);
            }
        }
    }

    /**
     * Sends the specified command with the given optional parameters. By default, an
     * EMSServerResponse containing the raw response is returned. If you wish to customize
     * this response, you don't need to override this method! Override the
     * handleServerResponse() method in this class instead, and you can parse out the
     * response and return some custom implementation of EMSServerResponse which contains your
     * parsed model object (or whatever). This means you have to extend the EMSServerResponse
     * class and override the parseResponse() method to parse out whatever you need.
     * <p>
     * If the server returned an error, then the EMSSServerResponse returned from this method
     * will contain the error message from the server.
     *
     * @param command The command to execute
     * @param params  An optional array of parameters for the command, or null if not connected.
     * @return A EMSServerResponse instance containing the results of the command, good or bad.
     */
    public EMSServerResponse sendCommand(String command, String... params) {
        if (!isConnected) {
            return null;
        }

        // Stupidity check:
        if (command == null || command.trim().isEmpty()) {
            return new EMSServerResponse("", EMSServer.UNRECOGNIZED_COMMAND);
        }

        // Form a command line from the given parameters:
        StringBuilder commandLineBuilder = new StringBuilder(command.trim().toUpperCase());
        if (params != null) {
            for (String param : params) {
                commandLineBuilder.append(EMSServer.DELIMITER);
                commandLineBuilder.append(param.trim());
            }
        }

        String commandLine = commandLineBuilder.toString();
        try {
            out.println(commandLine);
            StringBuilder rawResponse = new StringBuilder();
            boolean responseReceived = false;
            do {
                String responseLine = in.readLine();
                if (responseLine == null || EMSServer.DISCONNECTED.equals(responseLine)) {
                    logger.log(Level.SEVERE, "EMSClient disconnected.");
                    disconnect();
                    return new EMSServerResponse(commandLine, EMSServer.DISCONNECTED);
                }
                if (rawResponse.length() != 0) {
                    rawResponse.append("\n");
                }
                rawResponse.append(responseLine);

                // Note we expect all command handlers to terminate their response with
                // either OK or ERR, and if they don't, we're just going to loop forever here.
                // That's kind of dumb but we don't otherwise know when the response is over.
                // I suppose the server could send us the length of the response before
                // sending the response so we know when to stop reading it...
                if (responseLine.startsWith(EMSServer.RESPONSE_OK)
                        || responseLine.startsWith(EMSServer.RESPONSE_ERR)) {
                    responseReceived = true;
                }
            } while (!responseReceived);

            return handleServerResponse(commandLine, rawResponse.toString());
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "EMSClient received exception while sending command: " + ioe.getMessage(), ioe);
            disconnect();
            return new EMSServerResponse(commandLine, EMSServer.DISCONNECTED);
        }
    }

    /**
     * Generally, you shouldn't need to override this method, as the default implementation
     * will create a ServerResponse that parses out success/failure status along with any
     * message returned from the server. However, if your implementation has overridden
     * EMSServerResponse to contain more information, then you can override this method
     * and process the response however you please.
     *
     * @param commandLine The command line that was sent to the server before this response.
     * @param rawResponse The raw string response received from the server.
     * @return By default, an instance of EMSServerResponse, but you can override this behaviour
     * to return your own implementation of that class.
     */
    protected EMSServerResponse handleServerResponse(String commandLine, String rawResponse) {
        return new EMSServerResponse(commandLine, rawResponse);
    }

}
