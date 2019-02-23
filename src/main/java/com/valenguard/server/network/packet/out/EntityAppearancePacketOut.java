package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Appearance;
import com.valenguard.server.game.entity.Entity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

public class EntityAppearancePacketOut extends ServerAbstractOutPacket {

    public static final byte COLOR_INDEX = 0x01;
    public static final byte BODY_INDEX = 0x02;
    public static final byte HEAD_INDEX = 0x04;
    public static final byte ARMOR_INDEX = 0x08;
    public static final byte HELM_INDEX = 0x10;

    private final Entity entity;
    private final byte appearanceBits;

    public EntityAppearancePacketOut(Player receiver, Entity entity, byte appearanceBits) {
        super(Opcodes.APPEARANCE, receiver);
        this.entity = entity;
        this.appearanceBits = appearanceBits;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        write.writeShort(entity.getServerEntityId());
        write.writeByte(appearanceBits);
        Appearance appearance = entity.getAppearance();
        if ((appearanceBits & COLOR_INDEX) != 0) {
            write.writeByte(appearance.getColorId());
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