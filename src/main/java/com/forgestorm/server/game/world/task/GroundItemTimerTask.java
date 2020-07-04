package com.forgestorm.server.game.world.task;

import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.entity.ItemStackDrop;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.packet.out.EntitySpawnPacketOut;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.forgestorm.server.util.Log.println;

public class GroundItemTimerTask implements AbstractTask {

    private static final boolean PRINT_DEBUG = false;
    private static final int TIME_TO_SPAWN_TO_ALL = GameConstants.TICKS_PER_SECOND * 60; // 1 min
    private static final int TIME_TO_DESPAWN = GameConstants.TICKS_PER_SECOND * 60 * 3; // 3 min

    private final List<GroundItemTimer> groundItemTimers = new ArrayList<>();

    @Override
    public void tick(long ticksPassed) {
        Iterator<GroundItemTimer> itemTimerIterator = groundItemTimers.iterator();
        while (itemTimerIterator.hasNext()) {

            GroundItemTimer groundItemTimer = itemTimerIterator.next();

            if (groundItemTimer.itemStackDrop.isPickedUp()) {
                itemTimerIterator.remove();
            } else {
                if (groundItemTimer.timePassed == TIME_TO_DESPAWN) {
                    groundItemTimer.itemStackDrop.getGameMap().getItemStackDropEntityController().queueEntityDespawn(groundItemTimer.itemStackDrop);
                    itemTimerIterator.remove();
                }

                if (!groundItemTimer.itemStackDrop.isSpawnedForAll() && groundItemTimer.timePassed == TIME_TO_SPAWN_TO_ALL) {
                    for (Player player : groundItemTimer.itemStackDrop.getGameMap().getPlayerController().getPlayerList()) {
                        if (player.equals(groundItemTimer.itemStackDrop.getDropOwner())) continue;
                        new EntitySpawnPacketOut(player, groundItemTimer.itemStackDrop).sendPacket();
                    }
                    groundItemTimer.itemStackDrop.setSpawnedForAll(true);
                }
                groundItemTimer.timePassed++;
            }
            println(getClass(), "ItemStackID: " + groundItemTimer.itemStackDrop.getItemStack().getItemId() + ", TimePassed: " + groundItemTimer.timePassed, false, PRINT_DEBUG);
        }
    }

    public void addItemToGround(ItemStackDrop itemStackDrop) {
        // If this item was not spawned by a monster, then we skip this..
        if (!itemStackDrop.isSpawnedFromMonster()) return;
        groundItemTimers.add(new GroundItemTimer(itemStackDrop));
    }

    private class GroundItemTimer {

        private final ItemStackDrop itemStackDrop;
        private int timePassed = 0;

        GroundItemTimer(final ItemStackDrop itemStackDrop) {
            this.itemStackDrop = itemStackDrop;
        }
    }
}