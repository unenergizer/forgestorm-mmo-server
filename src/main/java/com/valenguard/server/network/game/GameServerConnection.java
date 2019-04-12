package com.valenguard.server.network.game;

import com.valenguard.server.Server;
import com.valenguard.server.io.NetworkSettingsLoader;
import com.valenguard.server.network.AuthenticationManager;
import com.valenguard.server.network.NetworkManager;
import com.valenguard.server.network.game.packet.out.GameOutputStream;
import com.valenguard.server.network.game.shared.ClientHandler;
import com.valenguard.server.network.game.shared.EventBus;
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
import java.util.UUID;
import java.util.function.Consumer;

import static com.valenguard.server.util.Log.println;

public class GameServerConnection {

    private final NetworkManager networkManager;

    @Getter
    private final EventBus eventBus = new EventBus();
    private ServerSocket serverSocket;

    /**
     * A temporary server ID assigned to players. This is
     * is different than the one stored in the database.
     */
    private short tempID = 0;

    /**
     * Used to handle closing down all threads associated with
     * the server. Volatile allows the variable to exist
     * between threads
     */
    @Setter
    @Getter
    private volatile boolean running = false;

    public GameServerConnection(final NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    /**
     * Opens a server on a given socket and registers event listeners.
     *
     * @param registerListeners Listeners to listen to.
     */
    public void openServer(NetworkSettingsLoader.NetworkSettings networkSettings, Consumer<EventBus> registerListeners) {

        // Creates a socket to allow for communication between clients and the server.
        try {
            serverSocket = new ServerSocket(networkSettings.getGamePort());
            println(getClass(), "Port Opened: " + networkSettings.getGamePort());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        running = true;
        registerListeners.accept(eventBus);
        eventBus.determineCanceling();
        listenForConnections();
    }

    /**
     * Listen for client connections and if possible establish
     * a link between the client and the server.
     */
    private void listenForConnections() {
        println(getClass(), "Listening for client connections...");

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
                }
            }
        }, "[" + getClass().getSimpleName() + "] GameConnectionListener").start();
    }

    /**
     * Start receiving packets for a new client connection.
     *
     * @param clientSocket A new client connection.
     */
    private void receivePackets(Socket clientSocket) {
        AuthenticationManager authenticationManager = networkManager.getAuthenticationManager();

        // This thread listens for incoming packets from the socket passed
        // to the method
        new Thread(() -> {
            ClientHandler clientHandler = null;
            // Using a new implementation in java that handles closing the streams
            // upon initialization. These streams are for sending and receiving io
            try (
                    DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
                    DataInputStream inStream = new DataInputStream(clientSocket.getInputStream())
            ) {

                // Grab player specific io from auth manager
                UUID uuid;
                try {
                    uuid = UUID.fromString(inStream.readUTF());
                } catch (IllegalArgumentException e) {
                    // The client sent a string that was not a UUID.
                    println(getClass(), e.getMessage(), true);
                    return;
                }
                boolean canConnect = authenticationManager.authGameUser(uuid, clientSocket.getInetAddress().getHostAddress());

                if (!canConnect) {
                    println(getClass(), "User UUID / IP didn't match!");
                    println(getClass(), "User UUID: " + uuid.toString());
                    println(getClass(), "User IP: " + clientSocket.getInetAddress().getHostAddress());
                    return;
                }

                // Creating a new client handle that contains the necessary components for
                // sending and receiving io
                clientHandler = new ClientHandler(authenticationManager.getDatabaseUserId(uuid), clientSocket, new GameOutputStream(outStream), inStream);

                String username = authenticationManager.getUsername(uuid);

                // Client handlers setup, remove entry from AuthenticationManager!
                authenticationManager.removeEntry(uuid);

                // Adding the client handle to a list of current client handles
                // TODO: this needs to be ran on the gamethread. Not the client's thread
                Server.getInstance().getCharacterManager().playerLogin(new PlayerSessionData(tempID, username, clientHandler));
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

                    for (byte i = 0; i < numberOfRepeats; i++) {
                        eventBus.decodeListenerOnNetworkThread(opcodeByte, clientHandler);
                    }
                }

            } catch (IOException e) {

                if (e instanceof EOFException || e instanceof SocketException || e instanceof SocketTimeoutException) {
                    // The user has logged out of the server
                    if (clientHandler != null && running) {

                        // The client has disconnected
                        if (!clientHandler.isPlayerQuitProcessed()) {
                            Server.getInstance().getGameManager().getPlayerProcessor().queuePlayerQuitServer(clientHandler);
                        }
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
        }, "[" + getClass().getSimpleName() + "] " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort()).start();
    }

    /**
     * Closing down the server's socket which means no more request from the clients may
     * be handled
     */
    public void close() {
        println(getClass(), "Closing down game connection...");
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
