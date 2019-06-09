package com.valenguard.server.database.sql;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.NPC;
import com.valenguard.server.game.world.maps.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameWorldNpcSQL {

    private void databaseLoad(NPC npc, ResultSet resultSet) throws SQLException {
        String worldName = resultSet.getString("world_name");
        short worldX = resultSet.getShort("world_x");
        short worldY = resultSet.getShort("world_y");
        String name = resultSet.getString("name");
        int health = resultSet.getInt("health");
        int damage = resultSet.getInt("damage");
        int expDrop = resultSet.getInt("exp_drop");
        int dropTable = resultSet.getInt("drop_table");
        float walkSpeed = resultSet.getFloat("walk_speed");
        float probStill = resultSet.getFloat("prob_still");
        float probWalk = resultSet.getFloat("prob_walk");
        short shopId = resultSet.getShort("shop_id");
        byte hairTexture = resultSet.getByte("hair_texture");
        byte helmTexture = resultSet.getByte("helm_texture");
        byte chestTexture = resultSet.getByte("chest_texture");
        byte pantsTexture = resultSet.getByte("pants_texture");
        byte shoesTexture = resultSet.getByte("shoes_texture");
        int hairColor = resultSet.getInt("hair_color");
        int eyesColor = resultSet.getInt("eye_color");
        int skinColor = resultSet.getInt("skin_color");
        int glovesColor = resultSet.getInt("gloves_color");

        Location location = new Location(worldName, worldX, worldY);

        npc.setCurrentMapLocation(location);
        npc.setFutureMapLocation(location);
        npc.setName(name);
        npc.setCurrentHealth(health);
        npc.setMaxHealth(health);
        npc.getAttributes().setDamage(damage);
        npc.setExpDrop(expDrop);
        npc.setDropTable(dropTable);
        npc.setMoveSpeed(walkSpeed);
        npc.setMovementInfo(probStill, probWalk, 0, 0, 96, 54);
        npc.setShopId(shopId);
        npc.getAppearance().setHairTexture(hairTexture);
        npc.getAppearance().setHelmTexture(helmTexture);
        npc.getAppearance().setChestTexture(chestTexture);
        npc.getAppearance().setPantsTexture(pantsTexture);
        npc.getAppearance().setShoesTexture(shoesTexture);
        npc.getAppearance().setHairColor(hairColor);
        npc.getAppearance().setEyeColor(eyesColor);
        npc.getAppearance().setSkinColor(skinColor);
        npc.getAppearance().setGlovesColor(glovesColor);
    }

    private PreparedStatement databaseSave(NPC npc, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game_world_npc" +
                " SET world_name=?, world_x=?, world_y=?, name=?, health=?, damage=?," +
                " exp_drop=?, drop_table=?, walk_speed=?, prob_still=?, prob_walk=?, shop_id=?," +
                " hair_texture=?, helm_texture=?, chest_texture=?, pants_texture=?, shoes_texture=?," +
                " hair_color=?, eye_color=?, skin_color=?, gloves_color=? WHERE npc_id=?");

        preparedStatement.setString(1, npc.getCurrentMapLocation().getMapName());
        preparedStatement.setInt(2, npc.getCurrentMapLocation().getX());
        preparedStatement.setInt(3, npc.getCurrentMapLocation().getY());
        preparedStatement.setString(4, npc.getName());
        preparedStatement.setInt(5, npc.getMaxHealth());
        preparedStatement.setInt(6, npc.getAttributes().getDamage());
        preparedStatement.setInt(7, npc.getExpDrop());
        preparedStatement.setInt(8, npc.getDropTable());
        preparedStatement.setFloat(9, npc.getMoveSpeed());
        preparedStatement.setFloat(10, npc.getRandomRegionMoveGenerator().getProbabilityStill());
        preparedStatement.setFloat(11, npc.getRandomRegionMoveGenerator().getProbabilityWalkStart());
        preparedStatement.setShort(12, npc.getShopId());
        preparedStatement.setByte(13, npc.getAppearance().getHairTexture());
        preparedStatement.setByte(14, npc.getAppearance().getHelmTexture());
        preparedStatement.setByte(15, npc.getAppearance().getChestTexture());
        preparedStatement.setByte(16, npc.getAppearance().getPantsTexture());
        preparedStatement.setByte(17, npc.getAppearance().getShoesTexture());
        preparedStatement.setInt(18, npc.getAppearance().getHairColor());
        preparedStatement.setInt(19, npc.getAppearance().getEyeColor());
        preparedStatement.setInt(20, npc.getAppearance().getSkinColor());
        preparedStatement.setInt(21, npc.getAppearance().getGlovesColor());
        preparedStatement.setInt(22, npc.getDatabaseId());

        return preparedStatement;
    }

    private PreparedStatement firstTimeSave(NPC npc, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_world_npc " +
                "(world_name, world_x, world_y, name, health, damage, exp_drop, drop_table, walk_speed, prob_still, prob_walk, shop_id, hair_texture, helm_texture, chest_texture, pants_texture, shoes_texture, hair_color, eye_color, skin_color, gloves_color) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        preparedStatement.setString(1, npc.getCurrentMapLocation().getMapName());
        preparedStatement.setShort(2, npc.getCurrentMapLocation().getX());
        preparedStatement.setShort(3, npc.getCurrentMapLocation().getY());
        preparedStatement.setString(4, npc.getName());
        preparedStatement.setInt(5, npc.getMaxHealth());
        preparedStatement.setInt(6, npc.getAttributes().getDamage());
        preparedStatement.setInt(7, npc.getExpDrop());
        preparedStatement.setInt(8, npc.getDropTable());
        preparedStatement.setFloat(9, npc.getMoveSpeed());
        preparedStatement.setFloat(10, npc.getRandomRegionMoveGenerator().getProbabilityStill());
        preparedStatement.setFloat(11, npc.getRandomRegionMoveGenerator().getProbabilityWalkStart());
        preparedStatement.setInt(12, npc.getShopId());
        preparedStatement.setByte(13, npc.getAppearance().getHairTexture());
        preparedStatement.setByte(14, npc.getAppearance().getHelmTexture());
        preparedStatement.setByte(15, npc.getAppearance().getChestTexture());
        preparedStatement.setByte(16, npc.getAppearance().getPantsTexture());
        preparedStatement.setByte(17, npc.getAppearance().getShoesTexture());
        preparedStatement.setInt(18, npc.getAppearance().getHairColor());
        preparedStatement.setInt(19, npc.getAppearance().getEyeColor());
        preparedStatement.setInt(20, npc.getAppearance().getSkinColor());
        preparedStatement.setInt(21, npc.getAppearance().getGlovesColor());

        return preparedStatement;
    }

    private SqlSearchData searchForData(NPC npc) {
        return new SqlSearchData("game_world_npc", "npc_id", npc.getDatabaseId());
    }

    public List<Integer> searchNPC(String worldName) {

        List<Integer> gameWorldNpcIds = new ArrayList<>();
        String query = "SELECT npc_id FROM game_world_npc WHERE world_name=?";

        try (Connection connection = Server.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, worldName);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int npc_id = resultSet.getInt("npc_id");

                gameWorldNpcIds.add(npc_id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return gameWorldNpcIds;
    }

    public void firstTimeSaveSQL(NPC npc) {
        PreparedStatement preparedStatement = null;
        try (Connection connection = Server.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            preparedStatement = firstTimeSave(npc, connection);
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

    public void loadSQL(NPC npc) {

        ResultSet resultSet = null;
        PreparedStatement searchStatement = null;
        PreparedStatement firstTimeSaveStatement = null;
        try (Connection connection = Server.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {

            SqlSearchData sqlSearchData = searchForData(npc);

            searchStatement = connection.prepareStatement("SELECT * FROM " + sqlSearchData.getTableName() + " WHERE " + sqlSearchData.getColumnName() + "=?");
            searchStatement.setObject(1, sqlSearchData.getSetData());
            resultSet = searchStatement.executeQuery();

            if (!resultSet.next()) {
                firstTimeSaveStatement = firstTimeSave(npc, connection);
                firstTimeSaveStatement.execute();
            } else {
                databaseLoad(npc, resultSet);
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

    public void saveSQL(NPC npc) {
        PreparedStatement preparedStatement = null;
        try (Connection connection = Server.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            preparedStatement = databaseSave(npc, connection);
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
