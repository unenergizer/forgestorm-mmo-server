package com.valenguard.server.network.packet.out;

import com.valenguard.server.network.shared.ClientHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class OutputStreamManager {

    private static final int MAX_BUFFER_SIZE = 500;

    private final Map<ClientHandler, Queue<ServerAbstractOutPacket>> outputContexts = new HashMap<>();

    public void sendPackets() {
        outputContexts.forEach((clientHandler, serverAbstractOutPackets) -> {
            int bufferOffsetCheck = 0;
            ServerAbstractOutPacket serverAbstractOutPacket;

            while ((serverAbstractOutPacket = serverAbstractOutPackets.poll()) != null) {

                int thisBufferSize = clientHandler.fillCurrentBuffer(serverAbstractOutPacket);
                bufferOffsetCheck += thisBufferSize;

                if (bufferOffsetCheck > MAX_BUFFER_SIZE) { // exceeds buffer limit so we should flush what we have written so far

                    // Writing any left over data that was not already written.
                    clientHandler.writeBuffers();
                    clientHandler.flushBuffer();

                    bufferOffsetCheck = thisBufferSize;

                    clientHandler.getValenguardOutputStream().createNewBuffers(serverAbstractOutPacket);
                    // This happened to be the last packet so we should add the
                    // to be written. Write and flush it.
                    if (serverAbstractOutPackets.peek() == null) {
                        clientHandler.writeBuffers();
                        clientHandler.flushBuffer();
                    }

                } else { // The current buffer fits into the current packet

                    ValenguardOutputStream valenguardOutputStream = clientHandler.getValenguardOutputStream();

                    if (!valenguardOutputStream.currentBuffersInitialized()) {
                        valenguardOutputStream.createNewBuffers(serverAbstractOutPacket);
                    } else {

                        boolean opcodesMatch = valenguardOutputStream.doOpcodesMatch(serverAbstractOutPacket);
                        if (opcodesMatch) {
                            valenguardOutputStream.appendBewBuffer();
                        } else {
                            clientHandler.writeBuffers();
                            valenguardOutputStream.createNewBuffers(serverAbstractOutPacket);
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
    }

    public void removeClient(ClientHandler clientHandler) {
        outputContexts.remove(clientHandler);
    }

    void addServerOutPacket(ServerAbstractOutPacket serverAbstractOutPacket) {
        Queue<ServerAbstractOutPacket> clientHandler = outputContexts.get(serverAbstractOutPacket.packetReceiver.getClientHandler());
        if (clientHandler != null) clientHandler.add(serverAbstractOutPacket);
    }
}
