package com.valenguard.server.database.sql;

import com.valenguard.server.Server;
import com.valenguard.server.game.character.CharacterDataOut;
import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.valenguard.server.util.Log.println;

public class GamePlayerCharacterSQL extends AbstractSingleSQL implements AbstractSQL {

    @Override
    public void databaseLoad(Player player, ResultSet resultSet) throws SQLException {
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

    @Override
    public PreparedStatement databaseSave(Player player, Connection connection) throws SQLException {
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
        preparedStatement.setInt(11, player.getCharacterDatabaseId());

        return preparedStatement;
    }

    @Override
    public PreparedStatement firstTimeSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_player_characters " +
                "(user_id, name, class, gender, race, faction, health, facing_direction, world_name, world_x, world_y, body_appearance, head_appearance, color_id) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        preparedStatement.setInt(1, player.getClientHandler().getDatabaseUserId());
        preparedStatement.setString(2, player.getName());
        preparedStatement.setInt(3, player.getCharacterClass().getTypeByte());
        preparedStatement.setInt(4, player.getCharacterGender().getTypeByte());
        preparedStatement.setInt(5, player.getCharacterRace().getTypeByte());
        preparedStatement.setInt(6, player.getFaction());
        preparedStatement.setInt(7, player.getCurrentHealth());
        preparedStatement.setString(8, player.getFacingDirection().toString());
        preparedStatement.setString(9, player.getCurrentMapLocation().getMapName());
        preparedStatement.setInt(10, player.getCurrentMapLocation().getX());
        preparedStatement.setInt(11, player.getCurrentMapLocation().getY());
        preparedStatement.setInt(12, player.getAppearance().getTextureId(Appearance.BODY));
        preparedStatement.setInt(13, player.getAppearance().getTextureId(Appearance.HEAD));
        preparedStatement.setInt(14, player.getAppearance().getColorId());

        return preparedStatement;
    }

    @Override
    public SqlSearchData searchForData(Player player) {
        return new SqlSearchData("game_player_characters", "character_id", player.getCharacterDatabaseId());
    }

    public List<CharacterDataOut> searchCharacters(int databaseUserId) {

        List<CharacterDataOut> characterDataOuts = new ArrayList<>();
        String query = "SELECT character_id, name, body_appearance, head_appearance, color_id FROM game_player_characters WHERE user_id = ?";

        try {
            Connection connection = Server.getInstance().getDatabaseManager().getHikariDataSource().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, databaseUserId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int characterId = resultSet.getInt("character_id");
                String name = resultSet.getString("name");
                short bodyId = resultSet.getByte("body_appearance");
                short headId = resultSet.getByte("head_appearance");
                byte colorId = resultSet.getByte("color_id");
                characterDataOuts.add(new CharacterDataOut(
                        (byte) characterId,
                        name,
                        bodyId,
                        headId,
                        colorId
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        println(getClass(), "Total Characters Loaded: " + characterDataOuts.size());
        return characterDataOuts;
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
