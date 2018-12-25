package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Appearance;
import com.valenguard.server.game.entity.Entity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.DataOutputStream;
import java.io.IOException;

public class EntityAppearancePacketOut extends ServerAbstractOutPacket {

    public static final int COLOR_INDEX = 0x01;
    public static final int BODY_INDEX = 0x02;
    public static final int HEAD_INDEX = 0x04;
    public static final int ARMOR_INDEX = 0x08;
    public static final int HELM_INDEX = 0x10;

    private final Entity entity;
    private final byte appearanceBits;

    public EntityAppearancePacketOut(Player receiver, Entity entity, byte appearanceBits) {
        super(Opcodes.APPEARANCE, receiver);
        this.entity = entity;
        this.appearanceBits = appearanceBits;
    }

    @Override
    protected void createPacket(DataOutputStream write) throws IOException {
        write.writeShort(entity.getServerEntityId());
        write.writeByte(appearanceBits);
        Appearance appearance = entity.getAppearance();
        if ((appearanceBits & COLOR_INDEX) != 0) {
            write.writeShort(appearance.getColorId());
        }
        if ((appearanceBits & BODY_INDEX) != 0) {
            write.writeShort(appearance.getTextureIds()[Appearance.BODY]);
        }
        if ((appearanceBits & HEAD_INDEX) != 0) {
            write.writeShort(appearance.getTextureIds()[Appearance.HEAD]);
        }
        if ((appearanceBits & ARMOR_INDEX) != 0) {
            write.writeShort(appearance.getTextureIds()[Appearance.ARMOR]);
        }
        if ((appearanceBits & HELM_INDEX) != 0) {
            write.writeShort(appearance.getTextureIds()[Appearance.HELM]);
        }
    }
}