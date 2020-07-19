package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.EntityType;
import com.forgestorm.server.game.world.entity.MovingEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.Opcodes;

import static com.forgestorm.server.util.Log.println;

public class EntityAttributesUpdatePacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;

    private final String entityName;
    private final short serverEntityId;
    private final EntityType entityType;
    private final int armor;
    private final int damage;

    public EntityAttributesUpdatePacketOut(final Player receiver, final MovingEntity movingEntity) {
        super(Opcodes.ATTRIBUTES_UPDATE, receiver.getClientHandler());

        this.entityName = movingEntity.getName();
        this.serverEntityId = movingEntity.getServerEntityId();
        this.entityType = detectEntityType(movingEntity);
        this.armor = movingEntity.getAttributes().getArmor();
        this.damage = movingEntity.getAttributes().getDamage();
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeShort(serverEntityId);
        write.writeByte(entityType.getEntityTypeByte());
        write.writeInt(armor);
        write.writeInt(damage);

        println(getClass(), "EntityName: " + entityName, false, PRINT_DEBUG);
        println(getClass(), "EntityId: " + serverEntityId, false, PRINT_DEBUG);
        println(getClass(), "EntityType: " + entityType, false, PRINT_DEBUG);
        println(getClass(), "Armor: " + armor, false, PRINT_DEBUG);
        println(getClass(), "Damage: " + damage, false, PRINT_DEBUG);
    }
}