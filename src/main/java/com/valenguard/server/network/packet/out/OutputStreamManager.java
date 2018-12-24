package com.valenguard.server.network.packet.out;

import java.util.LinkedList;
import java.util.Queue;

public class OutputStreamManager {

    private final Queue<ServerAbstractOutPacket> outputContexts = new LinkedList<>();

    public void sendPackets() {
        ServerAbstractOutPacket serverAbstractOutPacket;
        while ((serverAbstractOutPacket = outputContexts.poll()) != null) serverAbstractOutPacket.writeData();
    }

    void addServerOutPacket(ServerAbstractOutPacket serverAbstractOutPacket) {
        outputContexts.add(serverAbstractOutPacket);
    }
}
