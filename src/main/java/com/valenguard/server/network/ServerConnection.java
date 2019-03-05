package com.valenguard.server.network;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.network.packet.out.ValenguardOutputStream;
import com.valenguard.server.network.shared.ClientHandler;
import com.valenguard.server.network.shared.EventBus;
import com.valenguard.server.network.shared.NetworkSettings;
import com.valenguard.server.util.Log;
import lombok.Getter;
import lombok.Setter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.function.Consumer;

public class ServerConnection implements Runnable {

    private static ServerConnection instance;

    @Getter
    private final EventBus eventBus = new EventBus();
    private ServerSocket serverSocket;
    private Consumer<EventBus> registerListeners;

    private NetworkSettings networkSettings;

    private short tempID = 0;

    // Used to handle closing down all threads associated with
    // the server. Volatile allows the variable to exist
    // between threads
    @Setter
    @Getter
    private volatile boolean running = false;

    private ServerConnection() {
    }

    /**
     * Gets the main instance of this class.
     *
     * @return A singleton instance of this class.
     */
    public static ServerConnection getInstance() {
        if (instance == null) instance = new ServerConnection();
        return instance;
    }

    /**
     * Opens a server on a given socket and registers event listeners.
     *
     * @param registerListeners Listeners to listen to.
     */
    public void openServer(NetworkSettings networkSettings, Consumer<EventBus> registerListeners) {
        this.networkSettings = networkSettings;

        // Creates a socket to allow for communication between clients and the server.
        try {
            serverSocket = new ServerSocket(networkSettings.getPort());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // A callback for registering the listeners at a later time
        this.registerListeners = registerListeners;

        running = true;

        // Runs a thread for setting up
        new Thread(this, "NetworkThread").start();
    }

    /**
     * This is ran after the socket is setup. Request that the callback registers the listeners
     * that it needs and then calls a method to listen for incoming connections
     */
    @Override
    public void run() {
        Log.println(getClass(), "Server opened on port: " + networkSettings.getPort());
        registerListeners.accept(eventBus);
        eventBus.determineCanceling();
        listenForConnections();
    }

    /**
     * Listen for client connections and if possible establish
     * a link between the client and the server.
     */
    private void listenForConnections() {
        Log.println(getClass(), "Listening for client connections...");

        // Creating a thread that runs as long as the server is alive and listens
        // for incoming connections
        new Thread(() -> {
            while (running) {
                try {

                    // Listeners for a new client socket to connect
                    // to the server and throws the socket to a receive packets
                    // method to be handled
                    receivePackets(serverSocket.accept());

                } catch (IOException e) {

                    if (e instanceof SocketException && !running) {
                        break;
                    }

                    e.printStackTrace();
                    // End application here
                }
            }
        }, "ConnectionListener").start();
    }

    /**
     * Start receiving packets for a new client connection.
     *
     * @param clientSocket A new client connection.
     */
    private void receivePackets(Socket clientSocket) {

        // This thread listens for incoming packets from the socket passed
        // to the method
        new Thread(() -> {
            ClientHandler clientHandler = null;
            // Using a new implementation in java that handles closing the streams
            // upon initialization. These streams are for sending and receiving data
            try (
                    DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
                    DataInputStream inStream = new DataInputStream(clientSocket.getInputStream())
            ) {

                // Creating a new client handle that contains the necessary components for
                // sending and receiving data

                clientHandler = new ClientHandler(clientSocket, new ValenguardOutputStream(outStream), inStream);

                // Adding the client handle to a list of current client handles
                ValenguardMain.getInstance().getGameManager().initializeNewPlayer(new PlayerSessionData(tempID, new Credentials("TODO: UN", "TODO: PW"), clientHandler));
                tempID++;

                // Reading in a byte which represents an opcode that the client sent to the
                // server. Based on this opcode the event bus determines which listener should
                // be called
                while (running) {

                    byte opcodeByte = inStream.readByte();
                    byte numberOfRepeats = 1;
                    if (((opcodeByte >>> 8) & 0x01) != 0) {

                        // Removing the special bit.
                        opcodeByte = (byte) (opcodeByte & 0x7F);
                        numberOfRepeats = inStream.readByte();
                    }

                    for (byte i = 0; i < numberOfRepeats; i++)
                        eventBus.decodeListenerOnNetworkThread(opcodeByte, clientHandler);

                }

            } catch (IOException e) {

                if (e instanceof EOFException || e instanceof SocketException || e instanceof SocketTimeoutException) {
                    // The user has logged out of the server
                    if (clientHandler != null && running) {

                        // The client has disconnected
                        ValenguardMain.getInstance().getGameManager().queueClientQuitServer(clientHandler);
                    }
                } else {
                    e.printStackTrace();
                }

            } finally {
                // Closing the client socket for cleanup
                if (clientSocket != null) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } // Starting a new thread for the client in the format of address:port for the client and then starting the tread
        }, clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort()).start();
    }

    /**
     * Closing down the server's socket which means no more request from the clients may
     * be handled
     */
    public void close() {
        running = false;
        Log.println(getClass(), "Closing down server...");
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
