package com.valenguard.server.network.packet.out;

import java.util.LinkedList;
import java.util.Queue;

public class OutputStreamManager {

    private final Queue<ServerOutPacket> outputContexts = new LinkedList<>();

    public void sendPackets() {
        ServerOutPacket serverOutPacket;
        while ((serverOutPacket = outputContexts.poll()) != null) serverOutPacket.writeData();
    }

    void addServerOutPacket(ServerOutPacket serverOutPacket) {
        outputContexts.add(serverOutPacket);
    }
}
