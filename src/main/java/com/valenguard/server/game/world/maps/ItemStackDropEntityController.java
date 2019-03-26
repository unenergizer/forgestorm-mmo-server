package com.valenguard.server.game.world.maps;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.ItemStackDrop;
import com.valenguard.server.network.game.packet.out.EntitySpawnPacketOut;

public class ItemStackDropEntityController extends EntityController<ItemStackDrop> {

    ItemStackDropEntityController(GameMap gameMap) {
        super(gameMap);
    }

    @Override
    public void postEntityDespawn(ItemStackDrop entity) {
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
