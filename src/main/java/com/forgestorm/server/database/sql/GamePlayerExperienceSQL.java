package com.forgestorm.server.database.sql;

import com.forgestorm.server.game.world.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.forgestorm.server.util.Log.println;

public class GamePlayerExperienceSQL extends AbstractSingleSQL {

    private static final boolean PRINT_DEBUG = false;

    @Override
    void databaseLoad(Player player, ResultSet resultSet) throws SQLException {
        println(getClass(), "Loading EXP for " + player.getName(), false, PRINT_DEBUG);

        int melee = resultSet.getInt("attack_exp");
        int mining = resultSet.getInt("mining_exp");

        player.getSkills().MELEE.initExperience(melee);
        player.getSkills().MINING.initExperience(mining);

        println(getClass(), "LOADING MELEE: " + melee, false, PRINT_DEBUG);
        println(getClass(), "LOADING MINING: " + mining, false, PRINT_DEBUG);
    }

    @Override
    PreparedStatement databaseSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game_player_experience" +
                " SET attack_exp=?, mining_exp=? WHERE character_id=?");

        println(getClass(), "Saving EXP for " + player.getName(), false, PRINT_DEBUG);

        int melee = player.getSkills().MELEE.getExperience();
        int mining = player.getSkills().MINING.getExperience();

        preparedStatement.setInt(1, melee);
        preparedStatement.setInt(2, mining);
        preparedStatement.setInt(3, player.getDatabaseId());

        println(getClass(), "SAVING MELEE: " + melee, false, PRINT_DEBUG);
        println(getClass(), "SAVING MINING: " + mining, false, PRINT_DEBUG);

        return preparedStatement;
    }

    @Override
    PreparedStatement firstTimeSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_player_experience " +
                "(user_id, character_id, attack_exp, mining_exp) " +
                "VALUES(?, ?, ?, ?)");

        preparedStatement.setInt(1, player.getClientHandler().getAuthenticatedUser().getDatabaseUserId());
        preparedStatement.setInt(2, player.getDatabaseId());
        preparedStatement.setInt(3, 0);
        preparedStatement.setInt(4, 0);

        return preparedStatement;
    }

    @Override
    SqlSearchData searchForData(Player player) {
        return new SqlSearchData("game_player_experience", "character_id", player.getDatabaseId());
    }
}
