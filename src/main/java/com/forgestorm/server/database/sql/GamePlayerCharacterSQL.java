package com.forgestorm.server.database.sql;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.character.CharacterDataOut;
import com.forgestorm.server.game.world.entity.Appearance;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.shared.game.world.maps.MoveDirection;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.forgestorm.server.util.Log.println;

public class GamePlayerCharacterSQL extends AbstractSingleSQL implements AbstractSQL {

    private static final boolean PRINT_DEBUG = false;

    private final Date date = new Date();
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void databaseLoad(Player player, ResultSet resultSet) throws SQLException {
        player.setName(resultSet.getString("name"));
        player.setFaction(resultSet.getByte("faction"));
        player.setCurrentHealth(resultSet.getInt("health"));
        player.setFacingDirection(MoveDirection.valueOf(resultSet.getString("facing_direction")));
        Location loginLocation = new Location(resultSet.getString("world_name"),
                resultSet.getInt("world_x"),
                resultSet.getInt("world_y"),
                resultSet.getShort("world_z"));
        player.setCurrentWorldLocation(new Location(loginLocation));
        player.setFutureWorldLocation(new Location(loginLocation));

        byte hairTexture = resultSet.getByte("hair_texture");
        int hairColor = resultSet.getInt("hair_color");
        int eyeColor = resultSet.getInt("eye_color");
        int skinColor = resultSet.getInt("skin_color");

        Appearance appearance = player.getAppearance();
        appearance.setHairTexture(hairTexture);
        appearance.setHairColor(hairColor);
        appearance.setEyeColor(eyeColor);
        appearance.setSkinColor(skinColor);

        println(PRINT_DEBUG);
        println(getClass(), "==[ DATABASE LOAD ]====================================", false, PRINT_DEBUG);
        println(getClass(), "Name: " + player.getName(), false, PRINT_DEBUG);
        println(getClass(), "HairTexture: " + hairTexture, false, PRINT_DEBUG);
        println(getClass(), "HairColor: " + hairColor, false, PRINT_DEBUG);
        println(getClass(), "EyeColor: " + eyeColor, false, PRINT_DEBUG);
        println(getClass(), "SkinColor: " + skinColor, false, PRINT_DEBUG);
    }

    @Override
    public PreparedStatement databaseSave(Player player, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game_player_characters" +
                " SET name=?, faction=?, health=?, facing_direction=?, world_name=?, world_x=?, world_y=?, world_z=?," +
                " hair_texture=?, hair_color=?, eye_color=?, skin_color=? WHERE character_id=?");

        preparedStatement.setString(1, player.getName());
        preparedStatement.setInt(2, player.getFaction());
        preparedStatement.setInt(3, player.getCurrentHealth());
        preparedStatement.setString(4, player.getFacingDirection().toString());
        preparedStatement.setString(5, player.getWorldName());
        preparedStatement.setInt(6, player.getFutureWorldLocation().getX());
        preparedStatement.setInt(7, player.getFutureWorldLocation().getY());
        preparedStatement.setShort(8, player.getFutureWorldLocation().getZ());
        preparedStatement.setInt(9, player.getAppearance().getHairTexture());
        preparedStatement.setInt(10, player.getAppearance().getHairColor());
        preparedStatement.setInt(11, player.getAppearance().getEyeColor());
        preparedStatement.setInt(12, player.getAppearance().getSkinColor());
        preparedStatement.setInt(13, player.getDatabaseId());


        println(PRINT_DEBUG);
        println(getClass(), "==[ DATABASE SAVE ]====================================", false, PRINT_DEBUG);
        println(getClass(), "Name: " + player.getName(), false, PRINT_DEBUG);
        println(getClass(), "HairTexture: " + player.getAppearance().getHairTexture(), false, PRINT_DEBUG);
        println(getClass(), "HairColor: " + player.getAppearance().getHairColor(), false, PRINT_DEBUG);
        println(getClass(), "EyeColor: " + player.getAppearance().getEyeColor(), false, PRINT_DEBUG);
        println(getClass(), "SkinColor: " + player.getAppearance().getSkinColor(), false, PRINT_DEBUG);

        return preparedStatement;
    }

    @Override
    public PreparedStatement firstTimeSave(Player player, Connection connection) throws SQLException {
        String characterCreateDate = simpleDateFormat.format(date);
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_player_characters " +
                "(user_id, name, class, gender, race, faction, health, facing_direction, world_name, world_x, world_y, world_z, hair_texture, hair_color, eye_color, skin_color, create_date) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

        preparedStatement.setInt(1, player.getClientHandler().getAuthenticatedUser().getDatabaseUserId());
        preparedStatement.setString(2, player.getName());
        preparedStatement.setByte(3, player.getCharacterClass().getTypeByte());
        preparedStatement.setByte(4, player.getCharacterGender().getTypeByte());
        preparedStatement.setByte(5, player.getCharacterRace().getTypeByte());
        preparedStatement.setByte(6, player.getFaction());
        preparedStatement.setInt(7, player.getCurrentHealth());
        preparedStatement.setString(8, player.getFacingDirection().toString());
        preparedStatement.setString(9, player.getCurrentWorldLocation().getWorldName());
        preparedStatement.setInt(10, player.getCurrentWorldLocation().getX());
        preparedStatement.setInt(11, player.getCurrentWorldLocation().getY());
        preparedStatement.setShort(12, player.getCurrentWorldLocation().getZ());
        preparedStatement.setByte(13, player.getAppearance().getHairTexture());
        preparedStatement.setInt(14, player.getAppearance().getHairColor());
        preparedStatement.setInt(15, player.getAppearance().getEyeColor());
        preparedStatement.setInt(16, player.getAppearance().getSkinColor());
        preparedStatement.setString(17, characterCreateDate);

        println(PRINT_DEBUG);
        println(getClass(), "==[ FIRST TIME SAVE ]====================================", false, PRINT_DEBUG);
        println(getClass(), "Name: " + player.getName(), false, PRINT_DEBUG);
        println(getClass(), "HairTexture: " + player.getAppearance().getHairTexture(), false, PRINT_DEBUG);
        println(getClass(), "HairColor: " + player.getAppearance().getHairColor(), false, PRINT_DEBUG);
        println(getClass(), "EyeColor: " + player.getAppearance().getEyeColor(), false, PRINT_DEBUG);
        println(getClass(), "SkinColor: " + player.getAppearance().getSkinColor(), false, PRINT_DEBUG);

        return preparedStatement;
    }

    @Override
    public SqlSearchData searchForData(Player player) {
        return new SqlSearchData("game_player_characters", "character_id", player.getDatabaseId());
    }

    public List<CharacterDataOut> searchCharacters(int databaseUserId) {

        List<CharacterDataOut> characterDataOuts = new ArrayList<>();
        String query = "SELECT character_id, name, facing_direction, world_name, world_x, world_y, world_z, hair_texture, hair_color, eye_color, skin_color, is_deleted FROM game_player_characters WHERE user_id = ?";

        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, databaseUserId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int characterId = resultSet.getInt("character_id");
                String name = resultSet.getString("name");
                String facingDirection = resultSet.getString("facing_direction");
                String worldName = resultSet.getString("world_name");
                int worldX = resultSet.getInt("world_x");
                int worldY = resultSet.getInt("world_y");
                short worldZ = resultSet.getShort("world_z");
                byte hairTexture = resultSet.getByte("hair_texture");
                int hairColor = resultSet.getInt("hair_color");
                int eyeColor = resultSet.getInt("eye_color");
                int skinColor = resultSet.getInt("skin_color");
                boolean deletedCharacter = resultSet.getBoolean("is_deleted");

                println(PRINT_DEBUG);
                println(getClass(), "==[ CHARACTER SEARCH ]====================================", false, PRINT_DEBUG);
                println(getClass(), "Name: " + name, false, PRINT_DEBUG);
                println(getClass(), "FacingDirection: " + facingDirection, false, PRINT_DEBUG);
                println(getClass(), "World Name: " + worldName, false, PRINT_DEBUG);
                println(getClass(), "World X: " + worldX, false, PRINT_DEBUG);
                println(getClass(), "World Y: " + worldY, false, PRINT_DEBUG);
                println(getClass(), "World Z: " + worldZ, false, PRINT_DEBUG);
                println(getClass(), "HairTexture: " + hairTexture, false, PRINT_DEBUG);
                println(getClass(), "HairColor: " + hairColor, false, PRINT_DEBUG);
                println(getClass(), "EyeColor: " + eyeColor, false, PRINT_DEBUG);
                println(getClass(), "SkinColor: " + skinColor, false, PRINT_DEBUG);
                println(getClass(), "Deleted? " + deletedCharacter, false, PRINT_DEBUG);

                if (deletedCharacter) continue;
                characterDataOuts.add(new CharacterDataOut(
                        characterId,
                        name,
                        MoveDirection.valueOf(facingDirection),
                        worldName,
                        worldX,
                        worldY,
                        worldZ,
                        hairTexture,
                        hairColor,
                        eyeColor,
                        skinColor
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return characterDataOuts;
    }

    public void softDelete(Player player) {
        String query = "UPDATE game_player_characters SET is_deleted=?, delete_date=? WHERE character_id=?";

        String characterDeleteDate = simpleDateFormat.format(date);

        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setBoolean(1, true);
            preparedStatement.setString(2, characterDeleteDate);
            preparedStatement.setInt(3, player.getDatabaseId());

            preparedStatement.execute();
        } catch (SQLException exe) {
            exe.printStackTrace();
        }
    }

    public void firstTimeSaveSQL(Player player) {

        PreparedStatement preparedStatement = null;
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            preparedStatement = firstTimeSave(player, connection);
            preparedStatement.execute();

            // Set the players database ID
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int databaseId = generatedKeys.getInt(1);
                    player.setDatabaseId(databaseId);
                    println(getClass(), "Player Database ID: " + databaseId);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

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
