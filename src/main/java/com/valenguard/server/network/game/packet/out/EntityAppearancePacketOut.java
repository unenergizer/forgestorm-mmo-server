package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Entity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;

import static com.valenguard.server.util.Log.println;

public class EntityAppearancePacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;

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

                println(getClass(), "HairTexture: " + appearance.getHairTexture(), false, PRINT_DEBUG);
                println(getClass(), "HelmTexture: " + appearance.getHelmTexture(), false, PRINT_DEBUG);
                println(getClass(), "ChestTexture: " + appearance.getChestTexture(), false, PRINT_DEBUG);
                println(getClass(), "PantsTexture: " + appearance.getPantsTexture(), false, PRINT_DEBUG);
                println(getClass(), "ShoesTexture: " + appearance.getShoesTexture(), false, PRINT_DEBUG);
                println(getClass(), "HairColor: " + appearance.getHairColor(), false, PRINT_DEBUG);
                println(getClass(), "EyesColor: " + appearance.getEyeColor(), false, PRINT_DEBUG);
                println(getClass(), "SkinColor: " + appearance.getSkinColor(), false, PRINT_DEBUG);
                println(getClass(), "GlovesColor: " + appearance.getGlovesColor(), false, PRINT_DEBUG);
                break;
            case MONSTER:
            case ITEM_STACK:
            case SKILL_NODE:
                write.writeByte(appearance.getMonsterBodyTexture());

                println(getClass(), "MonsterBodyTexture: " + appearance.getMonsterBodyTexture(), false, PRINT_DEBUG);
                break;
        }
    }
}