package com.valenguard.server.game.world.maps;

import com.valenguard.server.Server;
import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.EntityType;
import com.valenguard.server.game.world.entity.ItemStackDrop;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.network.game.packet.out.EntitySpawnPacketOut;
import lombok.Getter;

public class ItemStackDropEntityController extends EntityController<ItemStackDrop> {

    @Getter
    private QueuedIdGenerator queuedIdGenerator = new QueuedIdGenerator(GameConstants.MAX_GROUND_ITEMS);

    ItemStackDropEntityController(GameMap gameMap) {
        super(gameMap);
    }

    @Override
    public void postEntityDespawn(ItemStackDrop entity) {
        queuedIdGenerator.deregisterId(entity);
    }

    public ItemStackDrop makeItemStackDrop(ItemStack itemStack, Location spawnLocation, Player dropOwner) {

        ItemStackDrop itemStackDrop = new ItemStackDrop();
        itemStackDrop.setEntityType(EntityType.ITEM_STACK);
        itemStackDrop.setName(itemStack.getName());
        itemStackDrop.setCurrentMapLocation(new Location(spawnLocation));
        itemStackDrop.setAppearance(new Appearance(itemStackDrop, (byte) 0, new short[]{(short) itemStack.getItemId()}));
        itemStackDrop.setItemStack(itemStack);
        itemStackDrop.setDropOwner(dropOwner);

        // Grabbing a free id for the ItemStack drop
        queuedIdGenerator.generateId(itemStackDrop);

        return itemStackDrop;
    }

    @Override
    public void tick() {
        entitySpawnQueue.forEach(itemStackDrop -> entityHashMap.put(itemStackDrop.getServerEntityId(), itemStackDrop));
        entityDespawnQueue.forEach(itemStackDrop -> entityHashMap.remove(itemStackDrop.getServerEntityId()));

        ItemStackDrop itemStackDrop;
        while ((itemStackDrop = entitySpawnQueue.poll()) != null) {
            Server.getInstance().getGameLoop().getGroundItemTimerTask().addItemToGround(itemStackDrop);
            new EntitySpawnPacketOut(itemStackDrop.getDropOwner(), itemStackDrop).sendPacket();
        }

        while ((itemStackDrop = entityDespawnQueue.poll()) != null) {
            entityDespawn(itemStackDrop);
        }
    }
}
