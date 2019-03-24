package com.valenguard.server.database.sql;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackManager;
import com.valenguard.server.game.world.item.inventory.EquipmentSlot;
import com.valenguard.server.util.Base64Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerInventorySQL extends AbstractSQL {

    private final ItemStackManager itemStackManager = Server.getInstance().getItemStackManager();

    @Override
    void databaseLoad(Player player, Connection connection, ResultSet resultSet) throws SQLException {
        ItemStack[] bagStack = (ItemStack[]) Base64Util.deserializeObjectFromBase64(resultSet.getString("bag"));
        setPlayerBag(player, bagStack);

        EquipmentSlot[] equipmentStack = (EquipmentSlot[]) Base64Util.deserializeObjectFromBase64(resultSet.getString("equipment"));
        setPlayerEquipment(player, equipmentStack);
    }

    private void setPlayerBag(Player player, ItemStack[] itemStacks) {
        for (byte i = 0; i < itemStacks.length; i++) {
            if (itemStacks[i] != null) player.setItemStack(i, itemStackManager.makeItemStack(itemStacks[i]));
        }
    }

    private void setPlayerEquipment(Player player, EquipmentSlot[] equipmentStack) {
        for (byte i = 0; i < equipmentStack.length; i++) {
            if (equipmentStack[i].getItemStack() != null) {
                player.getPlayerEquipment().setEquipmentSlot(i, itemStackManager.makeItemStack(equipmentStack[i].getItemStack()));
            }
        }
    }

    @Override
    PreparedStatement databaseSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game_player_inventory" +
                " SET bag=?, equipment=?, bank=? WHERE user_id=?");

        preparedStatement.setString(1, Base64Util.serializeObjectToBase64(player.getPlayerBag().getItems()));
        preparedStatement.setString(2, Base64Util.serializeObjectToBase64(player.getPlayerEquipment().getEquipmentSlots()));
        preparedStatement.setString(3, "penis");
        preparedStatement.setInt(4, player.getClientHandler().getDatabaseUserId());

        return preparedStatement;
    }

    @Override
    PreparedStatement firstTimeSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_player_inventory " +
                "(user_id, bag, equipment, bank) " +
                "VALUES(?, ?, ?, ?)");

        preparedStatement.setInt(1, player.getClientHandler().getDatabaseUserId());
        preparedStatement.setString(2, Base64Util.serializeObjectToBase64(player.getPlayerBag().getItems()));
        preparedStatement.setString(3, Base64Util.serializeObjectToBase64(player.getPlayerEquipment().getEquipmentSlots()));
        preparedStatement.setString(4, "penis");

        return preparedStatement;
    }

    @Override
    SqlSearchData searchForData(Player player, Connection connection) {
        return new SqlSearchData("game_player_inventory", "user_id", player.getClientHandler().getDatabaseUserId());
    }
}
