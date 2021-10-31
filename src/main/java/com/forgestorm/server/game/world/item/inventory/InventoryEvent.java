package com.forgestorm.server.game.world.item.inventory;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.game.world.item.inventory.InventoryMoveType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InventoryEvent {
    private Player player;
    private byte fromPositionIndex;
    private byte toPositionIndex;
    private InventoryMoveType inventoryMoveType;
}
