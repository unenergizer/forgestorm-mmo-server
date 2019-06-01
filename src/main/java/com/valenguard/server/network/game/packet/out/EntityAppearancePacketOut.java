package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Entity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;

public class EntityAppearancePacketOut extends AbstractServerOutPacket {

    private final Entity entity;

    public EntityAppearancePacketOut(final Player receiver, final Entity entity) {
        super(Opcodes.APPEARANCE, receiver.getClientHandler());
        this.entity = entity;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        Appearance appearance = entity.getAppearance();

        write.writeShort(entity.getServerEntityId());
        write.writeByte(getEntityType(entity).getEntityTypeByte());

        switch (entity.getEntityType()) {
            case CLIENT_PLAYER:
            case PLAYER:
            case NPC:
                write.writeByte(appearance.getHairTexture());
                write.writeByte(appearance.getHelmTexture());
                write.writeByte(appearance.getChestTexture());
                write.writeByte(appearance.getPantsTexture());
                write.writeByte(appearance.getShoesTexture());
                write.writeInt(appearance.getHairColor());
                write.writeInt(appearance.getEyeColor());
                write.writeInt(appearance.getSkinColor());
                write.writeInt(appearance.getGlovesColor());
                break;
            case MONSTER:
            case ITEM_STACK:
            case SKILL_NODE:
                write.writeByte(appearance.getMonsterBodyTexture());
                break;
        }
    }
}