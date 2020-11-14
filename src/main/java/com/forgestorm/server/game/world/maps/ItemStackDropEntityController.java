package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.entity.Appearance;
import com.forgestorm.server.game.world.entity.EntityType;
import com.forgestorm.server.game.world.entity.ItemStackDrop;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.item.ItemStack;

public class ItemStackDropEntityController extends EntityController<ItemStackDrop> {

    ItemStackDropEntityController(GameWorld gameWorld) {
        super(gameWorld, GameConstants.MAX_GROUND_ITEMS);
    }

    @Override
    public void postEntityDespawn(ItemStackDrop entity) {

    }

    public ItemStackDrop makeItemStackDrop(ItemStack itemStack, Location spawnLocation, Player dropOwner) {

        ItemStackDrop itemStackDrop = new ItemStackDrop();
        itemStackDrop.setEntityType(EntityType.ITEM_STACK);
        itemStackDrop.setName(itemStack.getName());
        itemStackDrop.setCurrentMapLocation(new Location(spawnLocation));
        Appearance appearance = new Appearance(itemStackDrop);
        itemStackDrop.setAppearance(appearance);
        appearance.setMonsterBodyTexture((byte) itemStack.getItemId());
        itemStackDrop.setItemStack(itemStack);
        itemStackDrop.setDropOwner(dropOwner);

        return itemStackDrop;
    }

    @Override
    public void tick() {
        entitySpawnQueue.forEach(itemStackDrop -> entityHashMap.put(itemStackDrop.getServerEntityId(), itemStackDrop));
        entityDespawnQueue.forEach(itemStackDrop -> entityHashMap.remove(itemStackDrop.getServerEntityId()));

        ItemStackDrop itemStackDrop;
        while ((itemStackDrop = entitySpawnQueue.poll()) != null) {
            ServerMain.getInstance().getGameLoop().getGroundItemTimerTask().addItemToGround(itemStackDrop);
//            new EntitySpawnPacketOut(itemStackDrop.getDropOwner(), itemStackDrop).sendPacket();
            entitySpawn(itemStackDrop);
        }

        while ((itemStackDrop = entityDespawnQueue.poll()) != null) {
            entityDespawn(itemStackDrop);
        }
    }
}
