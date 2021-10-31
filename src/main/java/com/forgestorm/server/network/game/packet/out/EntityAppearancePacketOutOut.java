package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Appearance;
import com.forgestorm.server.game.world.entity.Entity;
import com.forgestorm.server.game.world.entity.EntityType;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

import static com.forgestorm.server.util.Log.println;

public class EntityAppearancePacketOutOut extends AbstractPacketOut {

    private static final boolean PRINT_DEBUG = false;

    private final short serverEntityId;
    private final EntityType entityType;

    private final byte monsterBodyTexture;
    private final byte hairTexture;
    private final byte helmTexture;
    private final byte chestTexture;
    private final byte pantsTexture;
    private final byte shoesTexture;
    private final Integer hairColor;
    private final Integer eyeColor;
    private final Integer skinColor;
    private final int glovesColor;
    private final byte leftHandTexture;
    private final byte rightHandTexture;

    public EntityAppearancePacketOutOut(final Player receiver, final Entity entity) {
        super(Opcodes.APPEARANCE, receiver.getClientHandler());

        this.serverEntityId = entity.getServerEntityId();
        this.entityType = detectEntityType(entity);

        Appearance appearance = entity.getAppearance();
        this.monsterBodyTexture = appearance.getMonsterBodyTexture();
        this.hairTexture = appearance.getHairTexture();
        this.helmTexture = appearance.getHelmTexture();
        this.chestTexture = appearance.getChestTexture();
        this.pantsTexture = appearance.getPantsTexture();
        this.shoesTexture = appearance.getShoesTexture();
        this.hairColor = appearance.getHairColor();
        this.eyeColor = appearance.getEyeColor();
        this.skinColor = appearance.getSkinColor();
        this.glovesColor = appearance.getGlovesColor();
        this.leftHandTexture = appearance.getLeftHandTexture();
        this.rightHandTexture = appearance.getRightHandTexture();
    }

    @Override
    public void createPacket(GameOutputStream write) {
        write.writeShort(serverEntityId);
        write.writeByte(entityType.getEntityTypeByte());

        switch (entityType) {
            case CLIENT_PLAYER:
            case PLAYER:
            case NPC:
                write.writeByte(hairTexture);
                write.writeByte(helmTexture);
                write.writeByte(chestTexture);
                write.writeByte(pantsTexture);
                write.writeByte(shoesTexture);
                write.writeInt(hairColor);
                write.writeInt(eyeColor);
                write.writeInt(skinColor);
                write.writeInt(glovesColor);
                write.writeByte(leftHandTexture);
                write.writeByte(rightHandTexture);

                println(getClass(), "HairTexture: " + hairTexture, false, PRINT_DEBUG);
                println(getClass(), "HelmTexture: " + helmTexture, false, PRINT_DEBUG);
                println(getClass(), "ChestTexture: " + chestTexture, false, PRINT_DEBUG);
                println(getClass(), "PantsTexture: " + pantsTexture, false, PRINT_DEBUG);
                println(getClass(), "ShoesTexture: " + shoesTexture, false, PRINT_DEBUG);
                println(getClass(), "HairColor: " + hairColor, false, PRINT_DEBUG);
                println(getClass(), "EyesColor: " + eyeColor, false, PRINT_DEBUG);
                println(getClass(), "SkinColor: " + skinColor, false, PRINT_DEBUG);
                println(getClass(), "GlovesColor: " + glovesColor, false, PRINT_DEBUG);
                println(getClass(), "LeftHandTexture: " + leftHandTexture, false, PRINT_DEBUG);
                println(getClass(), "RightHandTexture: " + rightHandTexture, false, PRINT_DEBUG);
                break;
            case MONSTER:
            case ITEM_STACK:
            case SKILL_NODE:
                write.writeByte(monsterBodyTexture);

                println(getClass(), "MonsterBodyTexture: " + monsterBodyTexture, false, PRINT_DEBUG);
                break;
        }
    }
}