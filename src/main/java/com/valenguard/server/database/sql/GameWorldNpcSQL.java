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
        String name = resultSet.getString("name");
        String worldName = resultSet.getString("world_name");
        short worldX = resultSet.getShort("world_x");
        short worldY = resultSet.getShort("world_y");
        short regionStartX = resultSet.getShort("region_start_x");
        short regionStartY = resultSet.getShort("region_start_y");
        short regionEndX = resultSet.getShort("region_end_x");
        short regionEndY = resultSet.getShort("region_end_y");
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

        npc.setName(name);
        npc.setDefaultSpawnLocation(location);
        npc.setCurrentMapLocation(location);
        npc.setFutureMapLocation(location);
        npc.setRegionLocations(regionStartX, regionStartY, regionEndX, regionEndY);
        npc.setCurrentHealth(health);
        npc.setMaxHealth(health);
        npc.getAttributes().setDamage(damage);
        npc.setExpDrop(expDrop);
        npc.setDropTable(dropTable);
        npc.setMoveSpeed(walkSpeed);
        npc.setMovementInfo(probStill, probWalk);
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
                " SET name=?, world_name=?, world_x=?, world_y=?," +
                " region_start_x=?, region_end_x=?, region_start_y, region_end_y=?, " +
                " health=?, damage=?, exp_drop=?, drop_table=?, walk_speed=?, prob_still=?, prob_walk=?, shop_id=?," +
                " hair_texture=?, helm_texture=?, chest_texture=?, pants_texture=?, shoes_texture=?," +
                " hair_color=?, eye_color=?, skin_color=?, gloves_color=? WHERE npc_id=?");

        preparedStatement.setString(1, npc.getName());
        preparedStatement.setString(2, npc.getCurrentMapLocation().getMapName());
        preparedStatement.setShort(3, npc.getCurrentMapLocation().getX());
        preparedStatement.setShort(4, npc.getCurrentMapLocation().getY());
        preparedStatement.setShort(5, npc.getRegionStartX());
        preparedStatement.setShort(6, npc.getRegionEndX());
        preparedStatement.setShort(7, npc.getRegionStartY());
        preparedStatement.setShort(8, npc.getRegionEndY());
        preparedStatement.setInt(9, npc.getMaxHealth());
        preparedStatement.setInt(10, npc.getAttributes().getDamage());
        preparedStatement.setInt(11, npc.getExpDrop());
        preparedStatement.setInt(12, npc.getDropTable());
        preparedStatement.setFloat(13, npc.getMoveSpeed());
        preparedStatement.setFloat(14, npc.getRandomRegionMoveGenerator().getProbabilityStill());
        preparedStatement.setFloat(15, npc.getRandomRegionMoveGenerator().getProbabilityWalkStart());
        preparedStatement.setShort(16, npc.getShopId());
        preparedStatement.setByte(17, npc.getAppearance().getHairTexture());
        preparedStatement.setByte(18, npc.getAppearance().getHelmTexture());
        preparedStatement.setByte(19, npc.getAppearance().getChestTexture());
        preparedStatement.setByte(20, npc.getAppearance().getPantsTexture());
        preparedStatement.setByte(21, npc.getAppearance().getShoesTexture());
        preparedStatement.setInt(22, npc.getAppearance().getHairColor());
        preparedStatement.setInt(23, npc.getAppearance().getEyeColor());
        preparedStatement.setInt(24, npc.getAppearance().getSkinColor());
        preparedStatement.setInt(25, npc.getAppearance().getGlovesColor());
        preparedStatement.setInt(26, npc.getDatabaseId());

        return preparedStatement;
    }

    private PreparedStatement firstTimeSave(NPC npc, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_world_npc " +
                "(world_name, name, world_x, world_y, region_start_x, region_end_x, region_start_y, region_end_y, health, damage, exp_drop, drop_table, walk_speed, prob_still, prob_walk, shop_id, hair_texture, helm_texture, chest_texture, pants_texture, shoes_texture, hair_color, eye_color, skin_color, gloves_color) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        preparedStatement.setString(1, npc.getName());
        preparedStatement.setString(2, npc.getCurrentMapLocation().getMapName());
        preparedStatement.setShort(3, npc.getCurrentMapLocation().getX());
        preparedStatement.setShort(4, npc.getCurrentMapLocation().getY());
        preparedStatement.setShort(5, npc.getRegionStartX());
        preparedStatement.setShort(6, npc.getRegionEndX());
        preparedStatement.setShort(7, npc.getRegionStartY());
        preparedStatement.setShort(8, npc.getRegionEndY());
        preparedStatement.setInt(9, npc.getMaxHealth());
        preparedStatement.setInt(10, npc.getAttributes().getDamage());
        preparedStatement.setInt(11, npc.getExpDrop());
        preparedStatement.setInt(12, npc.getDropTable());
        preparedStatement.setFloat(13, npc.getMoveSpeed());
        preparedStatement.setFloat(14, npc.getRandomRegionMoveGenerator().getProbabilityStill());
        preparedStatement.setFloat(15, npc.getRandomRegionMoveGenerator().getProbabilityWalkStart());
        preparedStatement.setInt(16, npc.getShopId());
        preparedStatement.setByte(17, npc.getAppearance().getHairTexture());
        preparedStatement.setByte(18, npc.getAppearance().getHelmTexture());
        preparedStatement.setByte(19, npc.getAppearance().getChestTexture());
        preparedStatement.setByte(20, npc.getAppearance().getPantsTexture());
        preparedStatement.setByte(21, npc.getAppearance().getShoesTexture());
        preparedStatement.setInt(22, npc.getAppearance().getHairColor());
        preparedStatement.setInt(23, npc.getAppearance().getEyeColor());
        preparedStatement.setInt(24, npc.getAppearance().getSkinColor());
        preparedStatement.setInt(25, npc.getAppearance().getGlovesColor());

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

            // Now reload and spawn the entity
            Server.getInstance().getGameManager().getGameMapProcessor().loadNPC(npc.getGameMap());
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

    public void deleteSQL(NPC npc) {
        PreparedStatement preparedStatement = null;
        try (Connection connection = Server.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            preparedStatement = connection.prepareStatement("DELETE FROM game_world_npc WHERE npc_id=?");
            preparedStatement.setInt(1, npc.getDatabaseId());
            preparedStatement.executeUpdate();
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
