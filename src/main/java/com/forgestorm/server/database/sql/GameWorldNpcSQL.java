package com.forgestorm.server.database.sql;

import com.forgestorm.server.ServerMain;
import com.forgestorm.shared.game.world.entities.FirstInteraction;
import com.forgestorm.server.game.world.entity.NPC;
import com.forgestorm.server.game.world.maps.Location;

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
        int worldX = resultSet.getInt("world_x");
        int worldY = resultSet.getInt("world_y");
        short worldZ = resultSet.getShort("world_z");
        int regionStartX = resultSet.getInt("region_start_x");
        int regionStartY = resultSet.getInt("region_start_y");
        int regionEndX = resultSet.getInt("region_end_x");
        int regionEndY = resultSet.getInt("region_end_y");
        String firstInteract = resultSet.getString("first_interact");
        int health = resultSet.getInt("health");
        int damage = resultSet.getInt("damage");
        int expDrop = resultSet.getInt("exp_drop");
        int dropTable = resultSet.getInt("drop_table");
        float walkSpeed = resultSet.getFloat("walk_speed");
        float probStill = resultSet.getFloat("prob_still");
        float probWalk = resultSet.getFloat("prob_walk");
        short shopId = resultSet.getShort("shop_id");
        boolean isBankKeeper = resultSet.getBoolean("is_bank_keeper");
        byte factionId = resultSet.getByte("faction");
        byte hairTexture = resultSet.getByte("hair_texture");
        byte helmTexture = resultSet.getByte("helm_texture");
        byte chestTexture = resultSet.getByte("chest_texture");
        byte pantsTexture = resultSet.getByte("pants_texture");
        byte shoesTexture = resultSet.getByte("shoes_texture");
        int hairColor = resultSet.getInt("hair_color");
        int eyesColor = resultSet.getInt("eye_color");
        int skinColor = resultSet.getInt("skin_color");
        int glovesColor = resultSet.getInt("gloves_color");
        // TODO: byte scriptId = resultSet.getInt("script_id");
        int scriptId = 0;

        Location location = new Location(worldName, worldX, worldY, worldZ);

        npc.setName(name);
        npc.setDefaultSpawnLocation(location);
        npc.setCurrentWorldLocation(location);
        npc.setFutureWorldLocation(location);
        npc.setRegionLocations(regionStartX, regionStartY, regionEndX, regionEndY);
        if (firstInteract != null) {
            npc.setFirstInteraction(FirstInteraction.valueOf(firstInteract));
        }
        npc.setCurrentHealth(health);
        npc.setMaxHealth(health);
        npc.getAttributes().setDamage(damage);
        npc.setExpDrop(expDrop);
        npc.setDropTable(dropTable);
        npc.setMoveSpeed(walkSpeed);
        npc.setMovementInfo(probStill, probWalk);
        npc.setShopId(shopId);
        npc.setBankKeeper(isBankKeeper);
        npc.setFaction(factionId);
        npc.getAppearance().setHairTexture(hairTexture);
        npc.getAppearance().setHelmTexture(helmTexture);
        npc.getAppearance().setChestTexture(chestTexture);
        npc.getAppearance().setPantsTexture(pantsTexture);
        npc.getAppearance().setShoesTexture(shoesTexture);
        npc.getAppearance().setHairColor(hairColor);
        npc.getAppearance().setEyeColor(eyesColor);
        npc.getAppearance().setSkinColor(skinColor);
        npc.getAppearance().setGlovesColor(glovesColor);
        npc.setScriptId(scriptId);
    }

    private PreparedStatement databaseSave(NPC npc, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game_world_npc" +
                " SET name=?, first_interact=?, world_name=?, world_x=?, world_y=?, world_z=?," +
                " region_start_x=?, region_end_x=?, region_start_y=?, region_end_y=?, " +
                " health=?, damage=?, exp_drop=?, drop_table=?, walk_speed=?, prob_still=?, prob_walk=?, shop_id=?, is_bank_keeper=?, faction=?," +
                " hair_texture=?, helm_texture=?, chest_texture=?, pants_texture=?, shoes_texture=?," +
                " hair_color=?, eye_color=?, skin_color=?, gloves_color=? WHERE npc_id=?");

        preparedStatement.setString(1, npc.getName());
        preparedStatement.setString(2, npc.getFirstInteraction().toString());
        preparedStatement.setString(3, npc.getCurrentWorldLocation().getWorldName());
        preparedStatement.setInt(4, npc.getCurrentWorldLocation().getX());
        preparedStatement.setInt(5, npc.getCurrentWorldLocation().getY());
        preparedStatement.setShort(6, npc.getCurrentWorldLocation().getZ());
        preparedStatement.setInt(7, npc.getRegionStartX());
        preparedStatement.setInt(8, npc.getRegionEndX());
        preparedStatement.setInt(9, npc.getRegionStartY());
        preparedStatement.setInt(10, npc.getRegionEndY());
        preparedStatement.setInt(11, npc.getMaxHealth());
        preparedStatement.setInt(12, npc.getAttributes().getDamage());
        preparedStatement.setInt(13, npc.getExpDrop());
        preparedStatement.setInt(14, npc.getDropTable());
        preparedStatement.setFloat(15, npc.getMoveSpeed());
        preparedStatement.setFloat(16, npc.getRandomRegionMoveGenerator().getProbabilityStill());
        preparedStatement.setFloat(17, npc.getRandomRegionMoveGenerator().getProbabilityWalkStart());
        preparedStatement.setShort(18, npc.getShopId());
        preparedStatement.setBoolean(19, npc.isBankKeeper());
        preparedStatement.setByte(20, npc.getFaction());
        preparedStatement.setByte(21, npc.getAppearance().getHairTexture());
        preparedStatement.setByte(22, npc.getAppearance().getHelmTexture());
        preparedStatement.setByte(23, npc.getAppearance().getChestTexture());
        preparedStatement.setByte(24, npc.getAppearance().getPantsTexture());
        preparedStatement.setByte(25, npc.getAppearance().getShoesTexture());
        preparedStatement.setInt(26, npc.getAppearance().getHairColor());
        preparedStatement.setInt(27, npc.getAppearance().getEyeColor());
        preparedStatement.setInt(28, npc.getAppearance().getSkinColor());
        preparedStatement.setInt(29, npc.getAppearance().getGlovesColor());
        preparedStatement.setInt(30, npc.getDatabaseId());
        // TODO: preparedStatement.setInt(29, npc.getScriptId());

        return preparedStatement;
    }

    private PreparedStatement firstTimeSave(NPC npc, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_world_npc " +
                "(name, first_interact, world_name, world_x, world_y, world_z, region_start_x, region_end_x, region_start_y, region_end_y, health, damage, exp_drop, drop_table, walk_speed, prob_still, prob_walk, shop_id, is_bank_keeper, faction, hair_texture, helm_texture, chest_texture, pants_texture, shoes_texture, hair_color, eye_color, skin_color, gloves_color) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        preparedStatement.setString(1, npc.getName());
        preparedStatement.setString(2, npc.getFirstInteraction().toString());
        preparedStatement.setString(3, npc.getCurrentWorldLocation().getWorldName());
        preparedStatement.setInt(4, npc.getCurrentWorldLocation().getX());
        preparedStatement.setInt(5, npc.getCurrentWorldLocation().getY());
        preparedStatement.setShort(6, npc.getCurrentWorldLocation().getZ());
        preparedStatement.setInt(7, npc.getRegionStartX());
        preparedStatement.setInt(8, npc.getRegionEndX());
        preparedStatement.setInt(9, npc.getRegionStartY());
        preparedStatement.setInt(10, npc.getRegionEndY());
        preparedStatement.setInt(11, npc.getMaxHealth());
        preparedStatement.setInt(12, npc.getAttributes().getDamage());
        preparedStatement.setInt(13, npc.getExpDrop());
        preparedStatement.setInt(14, npc.getDropTable());
        preparedStatement.setFloat(15, npc.getMoveSpeed());
        preparedStatement.setFloat(16, npc.getRandomRegionMoveGenerator().getProbabilityStill());
        preparedStatement.setFloat(17, npc.getRandomRegionMoveGenerator().getProbabilityWalkStart());
        preparedStatement.setInt(18, npc.getShopId());
        preparedStatement.setBoolean(19, npc.isBankKeeper());
        preparedStatement.setByte(20, npc.getFaction());
        preparedStatement.setByte(21, npc.getAppearance().getHairTexture());
        preparedStatement.setByte(22, npc.getAppearance().getHelmTexture());
        preparedStatement.setByte(23, npc.getAppearance().getChestTexture());
        preparedStatement.setByte(24, npc.getAppearance().getPantsTexture());
        preparedStatement.setByte(25, npc.getAppearance().getShoesTexture());
        preparedStatement.setInt(26, npc.getAppearance().getHairColor());
        preparedStatement.setInt(27, npc.getAppearance().getEyeColor());
        preparedStatement.setInt(28, npc.getAppearance().getSkinColor());
        preparedStatement.setInt(29, npc.getAppearance().getGlovesColor());
        // TODO: preparedStatement.setInt(28, npc.getScriptId());

        return preparedStatement;
    }

    private SqlSearchData searchForData(NPC npc) {
        return new SqlSearchData("game_world_npc", "npc_id", npc.getDatabaseId());
    }

    public List<Integer> searchNPC(String worldName) {

        List<Integer> gameWorldNpcIds = new ArrayList<>();
        String query = "SELECT npc_id FROM game_world_npc WHERE world_name=?";

        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
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
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
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
            ServerMain.getInstance().getGameManager().getGameWorldProcessor().loadNPC(npc.getGameWorld());
        }
    }

    public void loadSQL(NPC npc) {

        ResultSet resultSet = null;
        PreparedStatement searchStatement = null;
        PreparedStatement firstTimeSaveStatement = null;
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {

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
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
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
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
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
