package com.valenguard.server.network.packet.out;

import com.valenguard.server.network.ServerConnection;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OutputStreamManager implements Runnable {

    private final Queue<ServerOutPacket> outputContexts = new ConcurrentLinkedQueue<>();

    @Override
    public void run() {
        while (ServerConnection.getInstance().isRunning()) {
            ServerOutPacket serverOutPacket;
            while ((serverOutPacket = outputContexts.poll()) != null) {
                serverOutPacket.writeData();
            }
        }
    }

    void addServerOutPacket(ServerOutPacket serverOutPacket) {
        outputContexts.add(serverOutPacket);
    }
}
