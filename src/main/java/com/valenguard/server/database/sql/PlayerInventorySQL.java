package com.valenguard.server.database.sql;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackManager;
import com.valenguard.server.game.world.item.inventory.InventorySlot;
import com.valenguard.server.util.Base64Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerInventorySQL extends AbstractSingleSQL {

    private final ItemStackManager itemStackManager = Server.getInstance().getItemStackManager();

    @Override
    void databaseLoad(Player player, ResultSet resultSet) throws SQLException {
        InventorySlot[] bagStack = (InventorySlot[]) Base64Util.deserializeObjectFromBase64(resultSet.getString("bag"));
        setPlayerBag(player, bagStack);

        InventorySlot[] equipmentStack = (InventorySlot[]) Base64Util.deserializeObjectFromBase64(resultSet.getString("equipment"));
        setPlayerEquipment(player, equipmentStack);
    }

    private void setPlayerBag(Player player, InventorySlot[] bagStacks) {
        if (bagStacks == null) return;
        for (byte slotIndex = 0; slotIndex < bagStacks.length; slotIndex++) {
            if (bagStacks[slotIndex].getItemStack() != null) {
                player.getPlayerBag().setItemStack(slotIndex, itemStackManager.makeItemStack(bagStacks[slotIndex].getItemStack()), false);
            }
        }
    }

    private void setPlayerEquipment(Player player, InventorySlot[] equipmentStacks) {
        for (byte slotIndex = 0; slotIndex < equipmentStacks.length; slotIndex++) {
            ItemStack itemStack = equipmentStacks[slotIndex].getItemStack();
            if (itemStack != null) {
                player.getPlayerEquipment().setEquipmentSlot(slotIndex, itemStackManager.makeItemStack(itemStack));
            }
        }
    }

    @Override
    PreparedStatement databaseSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game_player_inventory" +
                " SET bag=?, equipment=?, bank=? WHERE user_id=?");

        preparedStatement.setString(1, Base64Util.serializeObjectToBase64(player.getPlayerBag().getBagSlots()));
        preparedStatement.setString(2, Base64Util.serializeObjectToBase64(player.getPlayerEquipment().getEquipmentSlots()));
        preparedStatement.setString(3, "todo");
        preparedStatement.setInt(4, player.getClientHandler().getDatabaseUserId());

        return preparedStatement;
    }

    @Override
    PreparedStatement firstTimeSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_player_inventory " +
                "(user_id, bag, equipment, bank) " +
                "VALUES(?, ?, ?, ?)");

        preparedStatement.setInt(1, player.getClientHandler().getDatabaseUserId());
        preparedStatement.setString(2, Base64Util.serializeObjectToBase64(player.getPlayerBag().getBagSlots()));
        preparedStatement.setString(3, Base64Util.serializeObjectToBase64(player.getPlayerEquipment().getEquipmentSlots()));
        preparedStatement.setString(4, "todo");

        return preparedStatement;
    }

    @Override
    SqlSearchData searchForData(Player player) {
        return new SqlSearchData("game_player_inventory", "user_id", player.getClientHandler().getDatabaseUserId());
    }
}
