package com.valenguard.server.game.inventory;

import com.valenguard.server.game.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InventoryEvent {
    private Player player;
    private byte fromPosition;
    private byte toPosition;
    private WindowMovementInfo windowMovementInfo;
}
