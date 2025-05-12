package ca.corbett.ems.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created internally as needed by EMSServer to manager the server side of things.
 *
 * @author scorbo2
 * @since 2023-11-18
 */
class EMSServerThread extends Thread {

    private static final Logger logger = Logger.getLogger(EMSServerThread.class.getName());

    private final EMSServer emsServer;
    private final String hostname;
    private final int port;
    private final Map<String, EMSClientThread> clientConnections;
    private ServerSocket serverSocket;
    private volatile boolean isRunning;
    private final List<EMSServerSpy> serverSpies;

    EMSServerThread(EMSServer server, String hostname, int port) {
        this.emsServer = server;
        this.hostname = hostname;
        this.port = port;
        clientConnections = new HashMap<>();
        serverSpies = new ArrayList<>();
    }

    public void sendToClient(String clientId, String msg) {
        if (!isRunning) {
            return;
        }

        EMSClientThread clientThread = clientConnections.get(clientId);
        if (clientThread != null) {
            clientThread.send(msg);
        }
    }

    public void addServerSpy(EMSServerSpy spy) {
        serverSpies.add(spy);
        for (String clientId : clientConnections.keySet()) {
            EMSClientThread client = clientConnections.get(clientId);
            client.addServerSpy(spy);
        }
    }

    public void removeServerSpy(EMSServerSpy spy) {
        serverSpies.remove(spy);
        for (String clientId : clientConnections.keySet()) {
            EMSClientThread client = clientConnections.get(clientId);
            client.removeServerSpy(spy);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Starting server thread on port {0}", port);
        isRunning = true;
        try {
            serverSocket = new ServerSocket(port, 50, new InetSocketAddress(hostname, port).getAddress());
        } catch (IOException ioe) {
            isRunning = false;
            emsServer.setStartupException(ioe);
            logger.log(Level.SEVERE, "Caught exception on startup: {0}", ioe.getMessage());
            return;
        }

        try {
            while (!isInterrupted() && isRunning) {
                String clientId = getNextClientId();
                EMSClientThread client = new EMSClientThread(clientId, emsServer, serverSocket.accept());

                if (isInterrupted() || !isRunning) {
                    break;
                }

                // Notify server spies that a new client is starting up:
                for (EMSServerSpy spy : serverSpies) {
                    spy.clientConnected(emsServer, clientId);
                    client.addServerSpy(spy);
                }

                // Start it up:
                clientConnections.put(clientId, client);
                client.start();
            }
        } catch (IOException ioe) {
            if (!isInterrupted()) {
                logger.log(Level.SEVERE, "Caught exception; stopping server thread: {0}", ioe.getMessage());
            }
        }

        logger.log(Level.INFO, "Server thread stopped.");
    }

    @Override
    public void interrupt() {
        logger.info("Server interrupt request received; stopping all clients.");
        super.interrupt();
        for (String key : clientConnections.keySet()) {
            EMSClientThread client = clientConnections.get(key);
            if (client != null) {
                emsServer.sendToClient(client.getClientId(), EMSServer.DISCONNECTED);
                client.interrupt();
            }
        }
        clientConnections.clear();

        try {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Caught exception while shutting down server socket.", ioe);
        }
        isRunning = false;
    }

    private String getNextClientId() {
        return EMSServer.CLIENT_ID_PREFIX + String.format("%02d", clientConnections.size());
    }

}
