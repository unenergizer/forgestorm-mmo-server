package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.game.world.entity.Entity;

import java.util.LinkedList;
import java.util.Queue;

class QueuedIdGenerator {

    private Queue<Short> freedIds = new LinkedList<>();

    QueuedIdGenerator(short maxIdCount) {
        for (short i = 0; i < maxIdCount; i++) {
            freedIds.add(i);
        }
    }

    boolean generateId(Entity entity) {
        if (freedIds.isEmpty()) return false;
        entity.setServerEntityId(freedIds.remove());
        return true;
    }

    void deregisterId(Entity entity) {
        freedIds.add(entity.getServerEntityId());
    }
}
