package com.valenguard.server.game.task;

import com.valenguard.server.game.entity.ItemStackDrop;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.packet.out.EntitySpawnPacketOut;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ItemTickUpdates {

    private static final int TIME_TO_DESPAWN = 20 * 25;
    private static final int TIME_TO_SPAWN_TO_ALL = 20 * 10;

    private List<GroundItemTimer> groundItemTimers = new ArrayList<>();

    public void tickItemsDespawn() {
        Iterator<GroundItemTimer> itemTimerIterator = groundItemTimers.iterator();
        if (itemTimerIterator.hasNext()) {

            GroundItemTimer groundItemTimer = itemTimerIterator.next();

            if (groundItemTimer.itemStackDrop.isPickedUp()) {
                itemTimerIterator.remove();
            } else {
                if (groundItemTimer.timePassed > TIME_TO_DESPAWN) {
                    groundItemTimer.itemStackDrop.getGameMap().queueItemStackDropDespawn(groundItemTimer.itemStackDrop);
                    itemTimerIterator.remove();
                }

                if (!groundItemTimer.hasSpawnedForAll && groundItemTimer.timePassed > TIME_TO_SPAWN_TO_ALL) {
                    for (Player player : groundItemTimer.itemStackDrop.getGameMap().getPlayerList()) {
                        if (player.equals(groundItemTimer.itemStackDrop.getKiller())) continue;
                        new EntitySpawnPacketOut(player, groundItemTimer.itemStackDrop).sendPacket();
                    }
                    groundItemTimer.hasSpawnedForAll = true;
                }
                groundItemTimer.timePassed++;
            }
        }
    }

    public void addItemToGround(ItemStackDrop itemStackDrop) {
        groundItemTimers.add(new GroundItemTimer(itemStackDrop, 0, false));
    }

    @AllArgsConstructor
    private class GroundItemTimer {
        private ItemStackDrop itemStackDrop;
        private int timePassed;
        private boolean hasSpawnedForAll;
    }
}
