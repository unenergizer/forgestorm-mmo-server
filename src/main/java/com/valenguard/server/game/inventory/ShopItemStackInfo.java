package com.valenguard.server.game.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ShopItemStackInfo {
    private int itemId;
    private int price;
}