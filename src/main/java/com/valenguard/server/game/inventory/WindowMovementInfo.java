package com.valenguard.server.game.inventory;

public enum WindowMovementInfo {
    FROM_BAG_TO_BAG,
    FROM_BAG_TO_EQUIPMENT,
    FROM_EQUIPMENT_TO_BAG,
    FROM_EQUIPMENT_TO_EQUIPMENT;

    public InventoryType getFromWindow() {
        switch (this) {
            case FROM_BAG_TO_BAG:
                return InventoryType.BAG;
            case FROM_BAG_TO_EQUIPMENT:
                return InventoryType.BAG;
            case FROM_EQUIPMENT_TO_BAG:
                return InventoryType.EQUIPMENT;
            case FROM_EQUIPMENT_TO_EQUIPMENT:
                return InventoryType.EQUIPMENT;
        }
        throw new RuntimeException("Must implement all cases.");
    }
}
