package com.valenguard.server.database.sql;

import com.valenguard.server.game.world.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GamePlayerExperienceSQL extends AbstractSingleSQL {

    @Override
    void databaseLoad(Player player, ResultSet resultSet) throws SQLException {
        player.getSkills().MELEE.addExperience(resultSet.getInt("attack_exp"));
        player.getSkills().MINING.addExperience(resultSet.getInt("mining_exp"));
    }

    @Override
    PreparedStatement databaseSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game_player_experience" +
                " SET attack_exp=?, mining_exp=? WHERE character_id=?");

        preparedStatement.setInt(1, player.getSkills().MELEE.getExperience());
        preparedStatement.setInt(2, player.getSkills().MINING.getExperience());
        preparedStatement.setInt(3, player.getClientHandler().getDatabaseUserId());

        return preparedStatement;
    }

    @Override
    PreparedStatement firstTimeSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_player_experience " +
                "(user_id, character_id, attack_exp, mining_exp) " +
                "VALUES(?, ?, ?, ?)");

        preparedStatement.setInt(1, player.getClientHandler().getDatabaseUserId());
        preparedStatement.setInt(2, player.getCharacterId());
        preparedStatement.setInt(3, 0);
        preparedStatement.setInt(4, 0);

        return preparedStatement;
    }

    @Override
    SqlSearchData searchForData(Player player) {
        return new SqlSearchData("game_player_experience", "character_id", player.getCharacterId());
    }
}
