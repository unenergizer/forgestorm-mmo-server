package com.valenguard.server.game.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemStackSlotData {
    private ItemStack itemStack;
    private byte bagIndex;
}
