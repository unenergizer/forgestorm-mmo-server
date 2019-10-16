package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.world.entity.MovingEntity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;

import static com.valenguard.server.util.Log.println;

public class EntityAttributesUpdatePacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;

    private final MovingEntity movingEntity;

    public EntityAttributesUpdatePacketOut(final Player receiver, final MovingEntity movingEntity) {
        super(Opcodes.ATTRIBUTES_UPDATE, receiver.getClientHandler());
        this.movingEntity = movingEntity;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        Attributes attributes = movingEntity.getAttributes();
        write.writeShort(movingEntity.getServerEntityId());
        write.writeByte(getEntityType(movingEntity).getEntityTypeByte());
        write.writeInt(attributes.getArmor());
        write.writeInt(attributes.getDamage());

        println(getClass(), "Receiver: " + movingEntity.getName() + ", Type: " + movingEntity.getEntityType());
        println(getClass(), "Armor: " + attributes.getArmor(), false, PRINT_DEBUG);
        println(getClass(), "Damage: " + attributes.getDamage(), false, PRINT_DEBUG);
    }
}