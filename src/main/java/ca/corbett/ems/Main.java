package ca.corbett.ems;

import ca.corbett.ems.client.EMSClient;
import ca.corbett.ems.client.EMSServerResponse;
import ca.corbett.ems.server.EMSServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Simple command-line driver for EMS client and server, mostly for
 * testing purposes. For an actual application example that uses
 * EMS, see ems-example-app.
 * <p>
 *     Note: using highly simplified command line args here,
 *     as this is just a simple test driver. For a full
 *     example app, see the ems-example-app repo instead.
 * </p>
 * USAGE:
 * <BLOCKQUOTE>
 *     <PRE>java -jar ems.jar COMMAND [host [port]]</PRE>
 * </BLOCKQUOTE>
 * COMMAND is either "server" or "client". For example, to start a server
 * on localhost port 1555:
 * <BLOCKQUOTE>
 *     <PRE>java -jar ems.jar server localhost 1555</PRE>
 * </BLOCKQUOTE>
 * And to create a client for localhost port 1555:
 * <BLOCKQUOTE>
 *     <PRE>java -jar ems.jar client localhost 1555</PRE>
 * </BLOCKQUOTE>
 * If host is not specified, you get localhost. Port defaults to 1975.
 *
 * <p>
 *     Once started, the server will run until you ctrl+c it. Log info will be output
 *     to stdout so you can see what the server is sending and receiving.
 * </p>
 * <p>
 *     Once started, a client will run until you ctrl+c it. Anything you type will be
 *     sent to the server, and the server response will be output to stdout.
 * </p>
 *
 * @author scorbo2
 * @since 2025-03-16
 */
public class Main {

    private static void showUsageAndExit() {
        System.err.println("USAGE: java -jar ems.jar {server|client} {host} {port}");
        System.err.println("EXAMPLE: java -jar ems.jar server localhost 1555");
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            showUsageAndExit();
        }

        String startup = args[0];
        if (!"server".equals(startup) && !"client".equals(startup)) {
            showUsageAndExit();
        }

        String hostname = "0.0.0.0";
        int port = 1975;

        if (args.length >= 2) {
            hostname = args[1];
        }
        if (args.length == 3) {
            try {
                port = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {
                showUsageAndExit();
            }
        }

        if ("server".equals(startup)) {
            EMSServer server = new EMSServer(hostname, port);
            server.startServer();
        } else {
            EMSClient client = new EMSClient();
            System.out.print("Connecting to \""+hostname+"\" on port "+port+"...");
            if (client.connect(hostname, port)) {
                System.out.println();
                System.out.println("Connected. Type \"quit\" to disconnect or \"?\" for help.");
                System.out.print(">");
                try {
                    // Extremely basic command line parser follows!
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    String command;
                    do {
                        command = reader.readLine();
                        if (command != null && !command.trim().isEmpty()) {
                            command = command.trim();
                            if (command.equalsIgnoreCase("QUIT") || !client.isConnected()) {
                                System.out.println("Client disconnected.");
                                client.disconnect();
                                return;
                            }
                            String[] params = new String[0];
                            if (command.contains(EMSServer.DELIMITER)) {
                                String[] parts = command.split(EMSServer.DELIMITER);
                                command = parts[0];
                                params = new String[parts.length - 1];
                                for (int i = 1; i < parts.length; i++) {
                                    params[i - 1] = parts[i];
                                }
                            }
                            EMSServerResponse response = client.sendCommand(command, params);
                            String msg = response.getMessage();
                            if (!msg.isBlank()) {
                                System.out.print(msg);
                                if (! msg.endsWith("\n")) {
                                    System.out.println();
                                }
                            }
                            System.out.println(response.isError() ? EMSServer.RESPONSE_ERR : "");
                            System.out.print(">");
                            if (response.isServerDisconnectError()) {
                                client.disconnect();
                                break;
                            }
                        }
                    } while (command != null && !command.equalsIgnoreCase("QUIT"));
                }
                catch (IOException ioe) {
                    System.out.println("Error: caught exception: " + ioe.getMessage());
                    client.disconnect();
                }
            } else {
                System.err.println();
                System.err.println("Error: unable to connect to " + hostname + " on port " + port);
            }
        }
    }
}
