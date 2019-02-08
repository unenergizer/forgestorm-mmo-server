package com.valenguard.server.network.packet.out;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.entity.Player;
import lombok.Getter;

public abstract class ServerAbstractOutPacket {

    /**
     * Opcode to send with the out-going packet.
     */
    @Getter
    private final byte opcode;

    /**
     * The player who will receive the packet.
     */
    final Player player;

    ServerAbstractOutPacket(byte opcode, Player player) {
        this.opcode = opcode;
        this.player = player;
    }

    /**
     * Sends the packet to the player.
     */
    public void sendPacket() {
        ValenguardMain.getInstance().getOutStreamManager().addServerOutPacket(this);
    }

    /**
     * Creates the packet.
     */
    protected abstract void createPacket(ValenguardOutputStream write);
}
