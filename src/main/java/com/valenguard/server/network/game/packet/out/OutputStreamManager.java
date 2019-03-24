package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.network.game.shared.ClientHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class OutputStreamManager {

    private static final int MAX_BUFFER_SIZE = 500;

    private final Map<ClientHandler, Queue<AbstractServerOutPacket>> outputContexts = new HashMap<>();

    public void sendPackets() {
        outputContexts.forEach((clientHandler, serverAbstractOutPackets) -> {
            int bufferOffsetCheck = 0;
            AbstractServerOutPacket abstractServerOutPacket;

            while ((abstractServerOutPacket = serverAbstractOutPackets.poll()) != null) {

                int thisBufferSize = clientHandler.fillCurrentBuffer(abstractServerOutPacket);
                bufferOffsetCheck += thisBufferSize;

                if (bufferOffsetCheck > MAX_BUFFER_SIZE) { // exceeds buffer limit so we should flush what we have written so far

                    // Writing any left over data that was not already written.
                    clientHandler.writeBuffers();
                    clientHandler.flushBuffer();

                    bufferOffsetCheck = thisBufferSize;

                    clientHandler.getValenguardOutputStream().createNewBuffers(abstractServerOutPacket);
                    // This happened to be the last packet so we should add the
                    // to be written. Write and flush it.
                    if (serverAbstractOutPackets.peek() == null) {
                        clientHandler.writeBuffers();
                        clientHandler.flushBuffer();
                    }

                } else { // The current buffer fits into the current packet

                    ValenguardOutputStream valenguardOutputStream = clientHandler.getValenguardOutputStream();

                    if (!valenguardOutputStream.currentBuffersInitialized()) {
                        valenguardOutputStream.createNewBuffers(abstractServerOutPacket);
                    } else {

                        boolean opcodesMatch = valenguardOutputStream.doOpcodesMatch(abstractServerOutPacket);
                        if (opcodesMatch) {
                            valenguardOutputStream.appendBewBuffer();
                        } else {
                            clientHandler.writeBuffers();
                            valenguardOutputStream.createNewBuffers(abstractServerOutPacket);
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

    void addServerOutPacket(AbstractServerOutPacket abstractServerOutPacket) {
        Queue<AbstractServerOutPacket> clientHandler = outputContexts.get(abstractServerOutPacket.packetReceiver.getClientHandler());
        if (clientHandler != null) clientHandler.add(abstractServerOutPacket);
    }
}
