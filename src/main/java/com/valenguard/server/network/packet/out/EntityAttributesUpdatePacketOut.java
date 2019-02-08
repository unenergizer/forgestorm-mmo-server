package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.network.shared.Opcodes;

import static com.valenguard.server.util.Log.println;

public class EntityAttributesUpdatePacketOut extends ServerAbstractOutPacket {


    private final MovingEntity movingEntity;

    public EntityAttributesUpdatePacketOut(Player receiver, MovingEntity movingEntity) {
        super(Opcodes.ATTRIBUTES_UPDATE, receiver);
        this.movingEntity = movingEntity;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        Attributes attributes = movingEntity.getAttributes();
        write.writeShort(movingEntity.getServerEntityId());
        write.writeInt(attributes.getHealth());
        write.writeInt(attributes.getArmor());
        write.writeInt(attributes.getDamage());

        println(getClass(), "Health: " + attributes.getHealth());
        println(getClass(), "Armor: " + attributes.getArmor());
        println(getClass(), "Damage: " + attributes.getDamage());
    }
}