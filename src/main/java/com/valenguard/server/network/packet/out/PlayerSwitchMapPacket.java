package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.Warp;
import com.valenguard.server.network.shared.Opcodes;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class PlayerSwitchMapPacket extends ServerOutPacket {

    private final Warp warp;

    public PlayerSwitchMapPacket(Player player, Warp warp) {
        super(Opcodes.ENTITY_CHANGE_MAP, player);
        this.warp = warp;
    }

    @Override
    protected void createPacket(ObjectOutputStream write) throws IOException {
        write.writeUTF(warp.getLocation().getMapName());
    }
}
