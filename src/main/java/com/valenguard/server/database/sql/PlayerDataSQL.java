package com.valenguard.server.database.sql;

import com.valenguard.server.Server;
import com.valenguard.server.game.PlayerConstants;
import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerDataSQL extends AbstractSQL {

    @Override
    void databaseLoad(Player player, Connection connection, ResultSet resultSet) throws SQLException {
        player.setFaction(resultSet.getByte("faction"));
        player.setCurrentHealth(resultSet.getInt("health"));
        player.setFacingDirection(MoveDirection.valueOf(resultSet.getString("facing_direction")));
        Location loginLocation = new Location(resultSet.getString("world_name"), resultSet.getShort("world_x"), resultSet.getShort("world_y"));
        player.setCurrentMapLocation(new Location(loginLocation));
        player.setFutureMapLocation(new Location(loginLocation));


        short[] initialPlayerTextureIds = new short[4];
        initialPlayerTextureIds[Appearance.BODY] = resultSet.getShort("body_appearance");
        initialPlayerTextureIds[Appearance.HEAD] = resultSet.getShort("head_appearance");
        initialPlayerTextureIds[Appearance.ARMOR] = -1;
        initialPlayerTextureIds[Appearance.HELM] = -1;
        player.setAppearance(new Appearance(resultSet.getByte("color_id"), initialPlayerTextureIds));
    }

    @Override
    PreparedStatement databaseSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game_player" +
                " SET faction=?, health=?, facing_direction=?, world_name=?, world_x=?, world_y=?," +
                " body_appearance=?, head_appearance=?, color_id=? WHERE user_id=?");

        preparedStatement.setInt(1, player.getFaction());
        preparedStatement.setInt(2, player.getCurrentHealth());
        preparedStatement.setString(3, player.getFacingDirection().toString());
        preparedStatement.setString(4, player.getMapName());
        preparedStatement.setInt(5, player.getFutureMapLocation().getX());
        preparedStatement.setInt(6, player.getFutureMapLocation().getY());
        preparedStatement.setInt(7, player.getAppearance().getTextureId(Appearance.BODY));
        preparedStatement.setInt(8, player.getAppearance().getTextureId(Appearance.HEAD));
        preparedStatement.setInt(9, player.getAppearance().getColorId());
        preparedStatement.setInt(10, player.getClientHandler().getDatabaseUserId());

        return preparedStatement;
    }

    @Override
    PreparedStatement firstTimeSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_player " +
                "(user_id, faction, health, facing_direction, world_name, world_x, world_y, body_appearance, head_appearance, color_id) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        player.setFaction(Server.getInstance().getFactionManager().getFactionByName(PlayerConstants.STARTING_FACTION));
        player.setCurrentHealth(PlayerConstants.BASE_HP);
        player.setFacingDirection(PlayerConstants.STARTING_FACING_DIRECTION);
        player.setCurrentMapLocation(new Location(PlayerConstants.START_SPAWN_LOCATION));
        player.setFutureMapLocation(new Location(PlayerConstants.START_SPAWN_LOCATION));

        short[] initialPlayerTextureIds = new short[4];
        initialPlayerTextureIds[Appearance.BODY] = 0;
        initialPlayerTextureIds[Appearance.HEAD] = 0;
        initialPlayerTextureIds[Appearance.ARMOR] = -1;
        initialPlayerTextureIds[Appearance.HELM] = -1;
        player.setAppearance(new Appearance((byte) 0, initialPlayerTextureIds));

        preparedStatement.setInt(1, player.getClientHandler().getDatabaseUserId());
        preparedStatement.setInt(2, player.getFaction());
        preparedStatement.setInt(3, player.getCurrentHealth());
        preparedStatement.setString(4, player.getFacingDirection().toString());
        preparedStatement.setString(5, player.getCurrentMapLocation().getMapName());
        preparedStatement.setInt(6, player.getCurrentMapLocation().getX());
        preparedStatement.setInt(7, player.getCurrentMapLocation().getY());
        preparedStatement.setInt(8, player.getAppearance().getTextureId(Appearance.BODY));
        preparedStatement.setInt(9, player.getAppearance().getTextureId(Appearance.HEAD));
        preparedStatement.setInt(10, player.getAppearance().getColorId());

        return preparedStatement;
    }

    @Override
    SqlSearchData searchForData(Player player, Connection connection) {
        return new SqlSearchData("game_player", "user_id", player.getClientHandler().getDatabaseUserId());
    }
}
