package com.valenguard.server.database.sql;

import com.valenguard.server.game.world.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class GamePlayerReputationSQL extends AbstractVariantSQL<Integer> {

    @Override
    void databaseLoad(Player player, ResultSet resultSet) throws SQLException {
        short[] reputationData = player.getReputation().getReputationData();
        reputationData[resultSet.getByte("faction_id")] = resultSet.getShort("reputation");
    }

    @Override
    PreparedStatement firstTimeSave(Player player, Connection connection, Integer index) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_player_reputation " +
                "(user_id, character_id, faction_id, reputation) VALUES (?, ?, ?, ?)");

        preparedStatement.setInt(1, player.getClientHandler().getDatabaseUserId());
        preparedStatement.setInt(2, player.getCharacterDatabaseId());
        preparedStatement.setByte(3, index.byteValue());
        preparedStatement.setShort(4, player.getReputation().getReputationData()[index]);

        return preparedStatement;
    }

    @Override
    PreparedStatement[] databaseSave(Player player, Connection connection) throws SQLException {

        short[] reputationData = player.getReputation().getReputationData();
        PreparedStatement[] preparedStatements = new PreparedStatement[reputationData.length];

        for (byte factionIndex = 0; factionIndex < reputationData.length; factionIndex++) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "UPDATE game_player_reputation SET reputation=? WHERE character_id=? AND faction_id=?");

            preparedStatement.setShort(1, reputationData[factionIndex]);
            preparedStatement.setInt(2, player.getCharacterDatabaseId());
            preparedStatement.setByte(3, factionIndex);
            preparedStatements[factionIndex] = preparedStatement;
        }

        return preparedStatements;
    }

    @Override
    SqlSearchData searchForData(Player player) {
        return new SqlSearchData("game_player_reputation", "character_id", player.getCharacterDatabaseId());
    }

    @Override
    VariantData<Integer> getVariantData(Player player) {
        // Filling an array up with the faction IDs.
        short[] reputationData = player.getReputation().getReputationData();
        Integer[] factionIds = new Integer[reputationData.length];
        for (int factionId = 0; factionId < reputationData.length; factionId++) factionIds[factionId] = factionId;
        return new VariantData<>(Arrays.asList(factionIds), "faction_id");
    }
}
