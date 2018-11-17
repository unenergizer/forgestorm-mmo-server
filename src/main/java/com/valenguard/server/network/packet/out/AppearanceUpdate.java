package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class AppearanceUpdate extends ServerOutPacket {

    private final MovingEntity entity;

    public AppearanceUpdate(Player receiver, MovingEntity entity) {
        super(Opcodes.APPEARANCE, receiver);
        this.entity = entity;
    }

    @Override
    protected void createPacket(ObjectOutputStream write) throws IOException {
        write.writeShort(entity.getServerEntityId());
        write.writeShort(entity.getAppearance().getTextureId(0));
        write.writeShort(entity.getAppearance().getTextureId(1));
    }
}