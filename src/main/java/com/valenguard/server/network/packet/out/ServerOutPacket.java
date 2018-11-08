package com.valenguard.server.network.packet.out;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.ClientHandler;

import java.io.IOException;
import java.io.ObjectOutputStream;

public abstract class ServerOutPacket {

    /**
     * Opcode to send with the out-going packet.
     */
    private final byte opcode;

    /**
     * The player who will receive the packet.
     */
    final Player player;

    /**
     * The client handler that holds the object output stream
     */
    private final ClientHandler clientHandler;

    ServerOutPacket(byte opcode, Player player) {
        this.opcode = opcode;
        this.player = player;
        this.clientHandler = player.getClientHandler();
    }

    /**
     * Sends the packet to the player.
     */
    public void sendPacket() {
        ValenguardMain.getInstance().getOutStreamManager().addServerOutPacket(this);
    }

    void writeData() {
        clientHandler.write(opcode, this::createPacket);
    }

    /**
     * Creates the packet.
     */
    protected abstract void createPacket(ObjectOutputStream write) throws IOException;
}
