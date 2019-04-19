package com.valenguard.server.database.sql;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.inventory.InventorySlot;
import com.valenguard.server.util.Base64Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GamePlayerInventorySQL extends AbstractSingleSQL {

    @Override
    void databaseLoad(Player player, ResultSet resultSet) throws SQLException {
        InventorySlot[] inventorySlots = (InventorySlot[]) Base64Util.deserializeObjectFromBase64(resultSet.getString("bag"));
        player.getPlayerBag().setInventory(inventorySlots);

        inventorySlots = (InventorySlot[]) Base64Util.deserializeObjectFromBase64(resultSet.getString("equipment"));
        player.getPlayerEquipment().setInventory(inventorySlots);

        inventorySlots = (InventorySlot[]) Base64Util.deserializeObjectFromBase64(resultSet.getString("bank"));
        player.getPlayerBank().setInventory(inventorySlots);
    }

    @Override
    PreparedStatement databaseSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game_player_inventory" +
                " SET bag=?, equipment=?, bank=? WHERE character_id=?");

        preparedStatement.setString(1, Base64Util.serializeObjectToBase64(player.getPlayerBag().getInventorySlotArray()));
        preparedStatement.setString(2, Base64Util.serializeObjectToBase64(player.getPlayerEquipment().getInventorySlotArray()));
        preparedStatement.setString(3, Base64Util.serializeObjectToBase64(player.getPlayerBank().getInventorySlotArray()));
        preparedStatement.setInt(4, player.getCharacterDatabaseId());

        return preparedStatement;
    }

    @Override
    PreparedStatement firstTimeSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_player_inventory " +
                "(user_id, character_id, bag, equipment, bank) " +
                "VALUES(?, ?, ?, ?, ?)");

        preparedStatement.setInt(1, player.getClientHandler().getAuthenticatedUser().getDatabaseUserId());
        preparedStatement.setInt(2, player.getCharacterDatabaseId());
        preparedStatement.setString(3, Base64Util.serializeObjectToBase64(player.getPlayerBag().getInventorySlotArray()));
        preparedStatement.setString(4, Base64Util.serializeObjectToBase64(player.getPlayerEquipment().getInventorySlotArray()));
        preparedStatement.setString(5, Base64Util.serializeObjectToBase64(player.getPlayerBank().getInventorySlotArray()));

        return preparedStatement;
    }

    @Override
    SqlSearchData searchForData(Player player) {
        return new SqlSearchData("game_player_inventory", "character_id", player.getCharacterDatabaseId());
    }
}
