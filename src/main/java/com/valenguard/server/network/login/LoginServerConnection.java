package com.valenguard.server.network.login;

import com.valenguard.server.database.AuthenticatedUser;
import com.valenguard.server.database.sql.XFUserAuthenticateSQL;
import com.valenguard.server.io.NetworkSettingsLoader;
import com.valenguard.server.network.NetworkManager;
import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.SSLContext;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static com.valenguard.server.util.Log.println;

public class LoginServerConnection {

    private final NetworkManager networkManager;

    //    private SSLServerSocket serverSocket;
    private ServerSocket serverSocket;

    // Used to handle closing down all threads associated with
    // the server. Volatile allows the variable to exist
    // between threads
    @Setter
    @Getter
    private volatile boolean running = false;

    public LoginServerConnection(final NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public void openServer(NetworkSettingsLoader.NetworkSettings networkSettings) {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, null);
//            SSLServerSocketFactory factory = context.getServerSocketFactory();
//            serverSocket = (SSLServerSocket) factory.createServerSocket(networkSettings.getLoginPort());
            serverSocket = new ServerSocket(networkSettings.getLoginPort());

        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        println(getClass(), "Port Opened: " + networkSettings.getLoginPort());

        running = true;
        listenForConnections();
    }

    private void listenForConnections() {
        println(getClass(), "Listening for client connections...");

        new Thread(() -> {

            while (running) {
                try {
                    receivePackets(serverSocket.accept());
                } catch (IOException e) {

                    if (e instanceof SocketException && !running) {
                        break;
                    }

                    e.printStackTrace();
                }
            }

        }, "LoginConnectionListener").start();
    }

    private void receivePackets(Socket clientSocket) {

        new Thread(() -> {

            try (
                    DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                    DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream())
            ) {

                String xfAccountName = inputStream.readUTF();
                String xfPassword = inputStream.readUTF();

                LoginState loginState = XFUserAuthenticateSQL.authenticate(xfAccountName, xfPassword);

                outputStream.writeBoolean(loginState.getLoginSuccess());

                if (loginState.getLoginSuccess()) {
                    UUID uuid = UUID.randomUUID();

                    // Setup future connection in auth manager
                    networkManager.getAuthenticationManager().addLoginUser(uuid,
                            new AuthenticatedUser(clientSocket.getInetAddress().getHostAddress(), loginState.getUserId(), loginState.getUsername(), loginState.isAdmin()));

                    // Write Success io
                    outputStream.writeUTF(uuid.toString());
                } else {
                    // Write Failure io
                    outputStream.writeUTF(loginState.getFailReason());
                }

                outputStream.flush();
            } catch (IOException e) {
                if (!(e instanceof EOFException || e instanceof SocketException || e instanceof SocketTimeoutException)) {
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
            }
        }).start();
    }

    /**
     * Closing down the server's socket which means no more request from the clients may
     * be handled
     */
    public void close() {
        running = false;
        println(getClass(), "Closing down login connection...");
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
