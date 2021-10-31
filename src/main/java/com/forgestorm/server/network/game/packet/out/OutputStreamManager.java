package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.network.game.shared.ClientHandler;
import com.forgestorm.shared.network.game.GameOutputStream;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static com.forgestorm.server.util.Log.println;

public class OutputStreamManager {

    private static final boolean PRINT_DEBUG = false;
    private static final int MAX_BUFFER_SIZE = 500;

    private final Map<ClientHandler, Queue<AbstractPacketOut>> outputContexts = new HashMap<>();

    public void sendPackets() {
        outputContexts.forEach((clientHandler, serverAbstractOutPackets) -> {
            int bufferOffsetCheck = 0;
            AbstractPacketOut abstractPacketOut;

            while ((abstractPacketOut = serverAbstractOutPackets.poll()) != null) {

                println(getClass(), "PACKET OUT: " + abstractPacketOut, false, PRINT_DEBUG);

                int thisBufferSize = clientHandler.fillCurrentBuffer(abstractPacketOut);
                bufferOffsetCheck += thisBufferSize;

                if (bufferOffsetCheck > MAX_BUFFER_SIZE) { // exceeds buffer limit so we should flush what we have written so far

                    // Writing any left over io that was not already written.
                    clientHandler.writeBuffers();
                    clientHandler.flushBuffer();

                    bufferOffsetCheck = thisBufferSize;

                    clientHandler.getGameOutputStream().createNewBuffers(abstractPacketOut);
                    // This happened to be the last packet so we should add the
                    // to be written. Write and flush it.
                    if (serverAbstractOutPackets.peek() == null) {
                        clientHandler.writeBuffers();
                        clientHandler.flushBuffer();
                    }

                } else { // The current buffer fits into the current packet

                    GameOutputStream gameOutputStream = clientHandler.getGameOutputStream();

                    if (!gameOutputStream.currentBuffersInitialized()) {
                        gameOutputStream.createNewBuffers(abstractPacketOut);
                    } else {

                        boolean opcodesMatch = gameOutputStream.doOpcodesMatch(abstractPacketOut);
                        if (opcodesMatch) {
                            gameOutputStream.appendBewBuffer();
                        } else {
                            clientHandler.writeBuffers();
                            gameOutputStream.createNewBuffers(abstractPacketOut);
                        }
                    }

                    if (serverAbstractOutPackets.peek() == null) {
                        clientHandler.writeBuffers();
                        clientHandler.flushBuffer();
                    }
                }
            }
        });
    }

    public void addClient(ClientHandler clientHandler) {
        outputContexts.put(clientHandler, new LinkedList<>());
        println(getClass(), " + Server Join: " + clientHandler.getAuthenticatedUser().getXfAccountName());
        println(getClass(), " + Connected clients: " + clientsOnline());
    }

    public void removeClient(ClientHandler clientHandler) {
        println(getClass(), " - Server Quit: " + clientHandler.getAuthenticatedUser().getXfAccountName());
        outputContexts.remove(clientHandler);
        println(getClass(), " - Connected clients: " + clientsOnline());
    }

    void addServerOutPacket(AbstractPacketOut abstractPacketOut) {
        Queue<AbstractPacketOut> clientHandler = outputContexts.get(abstractPacketOut.clientHandler);
        if (clientHandler != null) clientHandler.add(abstractPacketOut);
    }

    public boolean isAccountOnline(String xfAccountName) {
        for (ClientHandler clientHandler : outputContexts.keySet()) {
            if (clientHandler.getAuthenticatedUser().getXfAccountName().equalsIgnoreCase(xfAccountName)) return true;
        }
        return false;
    }

    public int clientsOnline() {
        return outputContexts.size();
    }
}
