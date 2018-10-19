package com.valenguard.server.network.packet.out;

import com.valenguard.server.entity.Player;
import com.valenguard.server.network.shared.ClientHandler;

import java.io.IOException;
import java.io.ObjectOutputStream;

public abstract class ServerOutPacket {

    /**
     * Opcode to send with the out-going packet.
     */
    protected byte opcode;

    /**
     * The player who will receive the packet.
     */
    protected Player player;

    /**
     * The client handler that holds the object output stream
     */
    protected ClientHandler clientHandler;

    public ServerOutPacket(byte opcode, Player player) {
        this.opcode = opcode;
        this.player = player;
        this.clientHandler = player.getClientHandler();
    }

    /**
     * Sends the packet to the player.
     */
    public void sendPacket() {
        clientHandler.write(opcode, write -> createPacket(write));
    }

    /**
     * Creates the packet.
     */
    protected abstract void createPacket(ObjectOutputStream write) throws IOException;
}
