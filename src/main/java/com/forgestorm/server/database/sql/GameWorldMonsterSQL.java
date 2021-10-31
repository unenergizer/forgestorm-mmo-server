package com.forgestorm.server.database.sql;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.rpg.EntityAlignment;
import com.forgestorm.shared.game.world.entities.FirstInteraction;
import com.forgestorm.server.game.world.entity.Monster;
import com.forgestorm.server.game.world.maps.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameWorldMonsterSQL {

    private void databaseLoad(Monster monster, ResultSet resultSet) throws SQLException {
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
        String alignment = resultSet.getString("alignment");
        byte monsterBodyTexture = resultSet.getByte("monster_body_texture");

        Location location = new Location(worldName, worldX, worldY, worldZ);

        monster.setName(name);
        monster.setDefaultSpawnLocation(location);
        monster.setCurrentWorldLocation(location);
        monster.setFutureWorldLocation(location);
        monster.setRegionLocations(regionStartX, regionStartY, regionEndX, regionEndY);
        if (firstInteract != null) {
            monster.setFirstInteraction(FirstInteraction.valueOf(firstInteract));
        }
        monster.setCurrentHealth(health);
        monster.setMaxHealth(health);
        monster.getAttributes().setDamage(damage);
        monster.setExpDrop(expDrop);
        monster.setDropTable(dropTable);
        monster.setMoveSpeed(walkSpeed);
        monster.setMovementInfo(probStill, probWalk);
        monster.setShopId(shopId);
        monster.setAlignment(EntityAlignment.valueOf(alignment));
        monster.getAppearance().setMonsterBodyTexture(monsterBodyTexture);
    }

    private PreparedStatement databaseSave(Monster monster, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game_world_monster" +
                " SET name=?, first_interact=?, world_name=?, world_x=?, world_y=?, world_z=?, " +
                " region_start_x=?, region_end_x=?, region_start_y=?, region_end_y=?, " +
                " health=?, damage=?, exp_drop=?, drop_table=?, walk_speed=?, prob_still=?, prob_walk=?, shop_id=?," +
                " alignment=?, monster_body_texture=? WHERE monster_id=?");

        preparedStatement.setString(1, monster.getName());
        preparedStatement.setString(2, monster.getFirstInteraction().toString());
        preparedStatement.setString(3, monster.getCurrentWorldLocation().getWorldName());
        preparedStatement.setInt(4, monster.getCurrentWorldLocation().getX());
        preparedStatement.setInt(5, monster.getCurrentWorldLocation().getY());
        preparedStatement.setShort(6, monster.getCurrentWorldLocation().getZ());
        preparedStatement.setInt(7, monster.getRegionStartX());
        preparedStatement.setInt(8, monster.getRegionEndX());
        preparedStatement.setInt(9, monster.getRegionStartY());
        preparedStatement.setInt(10, monster.getRegionEndY());
        preparedStatement.setInt(11, monster.getMaxHealth());
        preparedStatement.setInt(12, monster.getAttributes().getDamage());
        preparedStatement.setInt(13, monster.getExpDrop());
        preparedStatement.setInt(14, monster.getDropTable());
        preparedStatement.setFloat(15, monster.getMoveSpeed());
        preparedStatement.setFloat(16, monster.getRandomRegionMoveGenerator().getProbabilityStill());
        preparedStatement.setFloat(17, monster.getRandomRegionMoveGenerator().getProbabilityWalkStart());
        preparedStatement.setShort(18, monster.getShopId());
        preparedStatement.setString(19, monster.getAlignment().toString());
        preparedStatement.setByte(20, monster.getAppearance().getMonsterBodyTexture());
        preparedStatement.setInt(21, monster.getDatabaseId());

        return preparedStatement;
    }

    private PreparedStatement firstTimeSave(Monster monster, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_world_monster " +
                "(name, first_interact, world_name, world_x, world_y, world_z, region_start_x, region_end_x, region_start_y, region_end_y, health, damage, exp_drop, drop_table, walk_speed, prob_still, prob_walk, shop_id, alignment, monster_body_texture) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        preparedStatement.setString(1, monster.getName());
        preparedStatement.setString(2, monster.getFirstInteraction().toString());
        preparedStatement.setString(3, monster.getCurrentWorldLocation().getWorldName());
        preparedStatement.setInt(4, monster.getCurrentWorldLocation().getX());
        preparedStatement.setInt(5, monster.getCurrentWorldLocation().getY());
        preparedStatement.setShort(6, monster.getCurrentWorldLocation().getZ());
        preparedStatement.setInt(7, monster.getRegionStartX());
        preparedStatement.setInt(8, monster.getRegionEndX());
        preparedStatement.setInt(9, monster.getRegionStartY());
        preparedStatement.setInt(10, monster.getRegionEndY());
        preparedStatement.setInt(11, monster.getMaxHealth());
        preparedStatement.setInt(12, monster.getAttributes().getDamage());
        preparedStatement.setInt(13, monster.getExpDrop());
        preparedStatement.setInt(14, monster.getDropTable());
        preparedStatement.setFloat(15, monster.getMoveSpeed());
        preparedStatement.setFloat(16, monster.getRandomRegionMoveGenerator().getProbabilityStill());
        preparedStatement.setFloat(17, monster.getRandomRegionMoveGenerator().getProbabilityWalkStart());
        preparedStatement.setInt(18, monster.getShopId());
        preparedStatement.setString(19, monster.getAlignment().toString());
        preparedStatement.setByte(20, monster.getAppearance().getMonsterBodyTexture());

        return preparedStatement;
    }

    private SqlSearchData searchForData(Monster monster) {
        return new SqlSearchData("game_world_monster", "monster_id", monster.getDatabaseId());
    }

    public List<Integer> searchMonster(String worldName) {

        List<Integer> gameWorldNpcIds = new ArrayList<>();
        String query = "SELECT monster_id FROM game_world_monster WHERE world_name=?";

        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, worldName);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int monster_id = resultSet.getInt("monster_id");

                gameWorldNpcIds.add(monster_id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return gameWorldNpcIds;
    }

    public void firstTimeSaveSQL(Monster monster) {
        PreparedStatement preparedStatement = null;
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            preparedStatement = firstTimeSave(monster, connection);
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
            ServerMain.getInstance().getGameManager().getGameWorldProcessor().loadMonster(monster.getGameWorld());
        }
    }

    public void loadSQL(Monster monster) {

        ResultSet resultSet = null;
        PreparedStatement searchStatement = null;
        PreparedStatement firstTimeSaveStatement = null;
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {

            SqlSearchData sqlSearchData = searchForData(monster);

            searchStatement = connection.prepareStatement("SELECT * FROM " + sqlSearchData.getTableName() + " WHERE " + sqlSearchData.getColumnName() + "=?");
            searchStatement.setObject(1, sqlSearchData.getSetData());
            resultSet = searchStatement.executeQuery();

            if (!resultSet.next()) {
                firstTimeSaveStatement = firstTimeSave(monster, connection);
                firstTimeSaveStatement.execute();
            } else {
                databaseLoad(monster, resultSet);
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

    public void saveSQL(Monster monster) {
        PreparedStatement preparedStatement = null;
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            preparedStatement = databaseSave(monster, connection);
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

    public void deleteSQL(Monster monster) {
        PreparedStatement preparedStatement = null;
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            preparedStatement = connection.prepareStatement("DELETE FROM game_world_monster WHERE monster_id=?");
            preparedStatement.setInt(1, monster.getDatabaseId());
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
