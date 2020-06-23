package com.valenguard.server.database.sql;

import com.valenguard.server.ServerMain;
import com.valenguard.server.game.rpg.EntityAlignment;
import com.valenguard.server.game.world.entity.Monster;
import com.valenguard.server.game.world.maps.Location;

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
        String alignment = resultSet.getString("alignment");
        byte monsterBodyTexture = resultSet.getByte("monster_body_texture");

        Location location = new Location(worldName, worldX, worldY);

        monster.setName(name);
        monster.setDefaultSpawnLocation(location);
        monster.setCurrentMapLocation(location);
        monster.setFutureMapLocation(location);
        monster.setRegionLocations(regionStartX, regionStartY, regionEndX, regionEndY);
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
                " SET name=?, world_name=?, world_x=?, world_y=?, " +
                " region_start_x=?, region_end_x=?, region_start_y=?, region_end_y=?, " +
                " health=?, damage=?, exp_drop=?, drop_table=?, walk_speed=?, prob_still=?, prob_walk=?, shop_id=?," +
                " alignment=?, monster_body_texture=? WHERE monster_id=?");

        preparedStatement.setString(1, monster.getName());
        preparedStatement.setString(2, monster.getCurrentMapLocation().getMapName());
        preparedStatement.setShort(3, monster.getCurrentMapLocation().getX());
        preparedStatement.setShort(4, monster.getCurrentMapLocation().getY());
        preparedStatement.setShort(5, monster.getRegionStartX());
        preparedStatement.setShort(6, monster.getRegionEndX());
        preparedStatement.setShort(7, monster.getRegionStartY());
        preparedStatement.setShort(8, monster.getRegionEndY());
        preparedStatement.setInt(9, monster.getMaxHealth());
        preparedStatement.setInt(10, monster.getAttributes().getDamage());
        preparedStatement.setInt(11, monster.getExpDrop());
        preparedStatement.setInt(12, monster.getDropTable());
        preparedStatement.setFloat(13, monster.getMoveSpeed());
        preparedStatement.setFloat(14, monster.getRandomRegionMoveGenerator().getProbabilityStill());
        preparedStatement.setFloat(15, monster.getRandomRegionMoveGenerator().getProbabilityWalkStart());
        preparedStatement.setShort(16, monster.getShopId());
        preparedStatement.setString(17, monster.getAlignment().toString());
        preparedStatement.setByte(18, monster.getAppearance().getMonsterBodyTexture());
        preparedStatement.setInt(19, monster.getDatabaseId());

        return preparedStatement;
    }

    private PreparedStatement firstTimeSave(Monster monster, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_world_monster " +
                "(name, world_name, world_x, world_y, region_start_x, region_end_x, region_start_y, region_end_y, health, damage, exp_drop, drop_table, walk_speed, prob_still, prob_walk, shop_id, alignment, monster_body_texture) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        preparedStatement.setString(1, monster.getName());
        preparedStatement.setString(2, monster.getCurrentMapLocation().getMapName());
        preparedStatement.setShort(3, monster.getCurrentMapLocation().getX());
        preparedStatement.setShort(4, monster.getCurrentMapLocation().getY());
        preparedStatement.setShort(5, monster.getRegionStartX());
        preparedStatement.setShort(6, monster.getRegionEndX());
        preparedStatement.setShort(7, monster.getRegionStartY());
        preparedStatement.setShort(8, monster.getRegionEndY());
        preparedStatement.setInt(9, monster.getMaxHealth());
        preparedStatement.setInt(10, monster.getAttributes().getDamage());
        preparedStatement.setInt(11, monster.getExpDrop());
        preparedStatement.setInt(12, monster.getDropTable());
        preparedStatement.setFloat(13, monster.getMoveSpeed());
        preparedStatement.setFloat(14, monster.getRandomRegionMoveGenerator().getProbabilityStill());
        preparedStatement.setFloat(15, monster.getRandomRegionMoveGenerator().getProbabilityWalkStart());
        preparedStatement.setInt(16, monster.getShopId());
        preparedStatement.setString(17, monster.getAlignment().toString());
        preparedStatement.setByte(18, monster.getAppearance().getMonsterBodyTexture());

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
            ServerMain.getInstance().getGameManager().getGameMapProcessor().loadMonster(monster.getGameMap());
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
