package ca.corbett.ems.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An internal class created as needed by EMSServer to monitor connected clients
 * for incoming commands and actually execute those commands.
 *
 * @author scorbo2
 * @since 2023-11-18
 */
class EMSClientThread extends Thread {

    private final static Logger logger = Logger.getLogger(EMSClientThread.class.getName());

    private final String clientId;
    private final EMSServer emsServer;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean isRunning;
    private final List<EMSServerSpy> serverSpies;

    public EMSClientThread(String id, EMSServer server, Socket socket) {
        this.clientId = id;
        this.emsServer = server;
        this.clientSocket = socket;
        serverSpies = new ArrayList<>();
    }

    public String getClientId() {
        return clientId;
    }

    public void send(String msg) {
        for (EMSServerSpy spy : serverSpies) {
            spy.messageSent(emsServer, clientId, msg);
        }
        if (out != null) { // can happen if this is invoked as we're shutting down or afterwards
            out.println(msg);
        }
    }

    public void addServerSpy(EMSServerSpy spy) {
        serverSpies.add(spy);
    }

    public void removeServerSpy(EMSServerSpy spy) {
        serverSpies.remove(spy);
    }

    @Override
    public void run() {
        isRunning = true;
        try {
            logger.log(Level.INFO, "Client thread {0} starting up", clientId);
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while (!isInterrupted() && isRunning) {
                if (!in.ready()) {
                    try {
                        Thread.sleep(500);
                        continue;
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }

                if (isInterrupted() || !isRunning) {
                    break;
                }

                String inputLine = in.readLine();
                if (inputLine == null || isInterrupted()) {
                    break;
                }

                // Special case for a client hang-up
                if (inputLine.trim().equals(EMSServer.DISCONNECTED)) {
                    break;
                }

                // Tell the server to execute the command and capture its output:
                String outputLine = emsServer.executeCommand(clientId, inputLine);

                // Notify any server spies that this has happened:
                for (EMSServerSpy spy : serverSpies) {
                    spy.messageReceived(emsServer, clientId, inputLine);
                    spy.messageSent(emsServer, clientId, outputLine);
                }

                // Dump it as-is to our out stream:
                out.println(outputLine);
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Caught exception; terminating client thread " + clientId, ioe);
        }

        if (clientSocket != null) {
            try {
                clientSocket.close();
                clientSocket = null;
                for (EMSServerSpy spy : serverSpies) {
                    spy.clientDisconnected(emsServer, clientId);
                }
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Client thread {0} was unable to shut down client socket.", clientId);
            }
        }

        logger.log(Level.INFO, "Client thread {0} terminated.", clientId);
    }

    @Override
    public void interrupt() {
        isRunning = false;
        super.interrupt();
    }

}
