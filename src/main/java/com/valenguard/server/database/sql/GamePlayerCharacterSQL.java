package com.valenguard.server.database.sql;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GamePlayerCharacterSQL implements AbstractSQL {

    private void databaseLoad(Player player, ResultSet resultSet) throws SQLException {
        player.setName(resultSet.getString("name"));
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
        player.setAppearance(new Appearance(player, resultSet.getByte("color_id"), initialPlayerTextureIds));
    }

    private PreparedStatement databaseSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game_player_characters" +
                " SET name=?, faction=?, health=?, facing_direction=?, world_name=?, world_x=?, world_y=?," +
                " body_appearance=?, head_appearance=?, color_id=? WHERE character_id=?");

        preparedStatement.setString(1, player.getName());
        preparedStatement.setInt(2, player.getFaction());
        preparedStatement.setInt(3, player.getCurrentHealth());
        preparedStatement.setString(4, player.getFacingDirection().toString());
        preparedStatement.setString(5, player.getMapName());
        preparedStatement.setInt(6, player.getFutureMapLocation().getX());
        preparedStatement.setInt(7, player.getFutureMapLocation().getY());
        preparedStatement.setInt(8, player.getAppearance().getTextureId(Appearance.BODY));
        preparedStatement.setInt(9, player.getAppearance().getTextureId(Appearance.HEAD));
        preparedStatement.setInt(10, player.getAppearance().getColorId());
        preparedStatement.setInt(11, player.getCharacterId());

        return preparedStatement;
    }

    private PreparedStatement firstTimeSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_player_characters " +
                "(user_id, name, faction, health, facing_direction, world_name, world_x, world_y, body_appearance, head_appearance, color_id) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        preparedStatement.setInt(1, player.getClientHandler().getDatabaseUserId());
        preparedStatement.setString(2, player.getName());
        preparedStatement.setInt(3, player.getFaction());
        preparedStatement.setInt(4, player.getCurrentHealth());
        preparedStatement.setString(5, player.getFacingDirection().toString());
        preparedStatement.setString(6, player.getCurrentMapLocation().getMapName());
        preparedStatement.setInt(7, player.getCurrentMapLocation().getX());
        preparedStatement.setInt(8, player.getCurrentMapLocation().getY());
        preparedStatement.setInt(9, player.getAppearance().getTextureId(Appearance.BODY));
        preparedStatement.setInt(10, player.getAppearance().getTextureId(Appearance.HEAD));
        preparedStatement.setInt(11, player.getAppearance().getColorId());

        return preparedStatement;
    }

    private SqlSearchData searchForData(Player player) {
        return new SqlSearchData("game_player_characters", "character_id", player.getCharacterId());
    }

    @Override
    public void loadSQL(Player player) {

        ResultSet resultSet = null;
        PreparedStatement searchStatement = null;
        PreparedStatement firstTimeSaveStatement = null;
        try (Connection connection = Server.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {

            SqlSearchData sqlSearchData = searchForData(player);

            searchStatement = connection.prepareStatement("SELECT * FROM " + sqlSearchData.getTableName() + " WHERE " + sqlSearchData.getColumnName() + "=?");
            searchStatement.setObject(1, sqlSearchData.getSetData());
            resultSet = searchStatement.executeQuery();

            if (!resultSet.next()) {
                firstTimeSaveStatement = firstTimeSave(player, connection);
                firstTimeSaveStatement.execute();
            } else {
                databaseLoad(player, resultSet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (searchStatement != null) searchStatement.close();
                if (firstTimeSaveStatement != null) firstTimeSaveStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveSQL(Player player) {
        PreparedStatement preparedStatement = null;
        try (Connection connection = Server.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            preparedStatement = databaseSave(player, connection);
            preparedStatement.execute();
        } catch (SQLException exe) {
            exe.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void firstTimeSaveSQL(Player player) {
        PreparedStatement preparedStatement = null;
        try (Connection connection = Server.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            preparedStatement = firstTimeSave(player, connection);
            preparedStatement.execute();
        } catch (SQLException exe) {
            exe.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
