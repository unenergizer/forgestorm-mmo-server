package com.forgestorm.server.database.sql;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.entity.Appearance;
import com.forgestorm.server.game.world.entity.EntityType;
import com.forgestorm.server.game.world.entity.ItemStackDrop;
import com.forgestorm.server.game.world.item.ItemStack;
import com.forgestorm.server.game.world.maps.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameWorldItemStackDropSQL {

    private void databaseLoad(ItemStackDrop itemStackDrop, ResultSet resultSet) throws SQLException {
        String worldName = resultSet.getString("world_name");
        int worldX = resultSet.getInt("world_x");
        int worldY = resultSet.getInt("world_y");
        short worldZ = resultSet.getShort("world_z");
        int itemStackId = resultSet.getInt("itemstack_id");
        int amount = resultSet.getInt("amount");
        int respawnTimeMin = resultSet.getInt("respawn_time_min");
        int respawnTimeMax = resultSet.getInt("respawn_time_min");

        Location location = new Location(worldName, worldX, worldY, worldZ);
        ItemStack itemStack = ServerMain.getInstance().getItemStackManager().makeItemStack(itemStackId, amount);

        itemStackDrop.setSpawnedForAll(true);
        itemStackDrop.setSpawnedFromMonster(false);
        itemStackDrop.setEntityType(EntityType.ITEM_STACK);
        itemStackDrop.setName(itemStack.getName());
        itemStackDrop.setCurrentWorldLocation(location);
        itemStackDrop.setItemStack(itemStack);
        itemStackDrop.setRespawnTimeMin(respawnTimeMin);
        itemStackDrop.setRespawnTimeMax(respawnTimeMax);
        Appearance appearance = new Appearance(itemStackDrop);
        itemStackDrop.setAppearance(appearance);
        appearance.setMonsterBodyTexture((byte) itemStack.getItemId());
    }

    private PreparedStatement databaseSave(ItemStackDrop itemStackDrop, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE game_world_itemstack_drop" +
                " SET world_name=?, world_x=?, world_y=?, world_z=?, " +
                " itemstack_id=?, amount=?, respawn_time_min=?, respawn_time_max=? " +
                " WHERE drop_id=?");

        preparedStatement.setString(1, itemStackDrop.getCurrentWorldLocation().getWorldName());
        preparedStatement.setInt(2, itemStackDrop.getCurrentWorldLocation().getX());
        preparedStatement.setInt(3, itemStackDrop.getCurrentWorldLocation().getY());
        preparedStatement.setShort(4, itemStackDrop.getCurrentWorldLocation().getZ());
        preparedStatement.setInt(5, itemStackDrop.getItemStack().getItemId());
        preparedStatement.setInt(6, itemStackDrop.getItemStack().getAmount());
        preparedStatement.setInt(7, itemStackDrop.getRespawnTimeMin());
        preparedStatement.setInt(8, itemStackDrop.getRespawnTimeMax());
        preparedStatement.setInt(9, itemStackDrop.getDatabaseId());

        return preparedStatement;
    }

    private PreparedStatement firstTimeSave(ItemStackDrop itemStackDrop, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO game_world_itemstack_drop " +
                "(world_name, world_x, world_y, world_z, itemstack_id, amount, respawn_time_min, respawn_time_max) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)");

        preparedStatement.setString(1, itemStackDrop.getCurrentWorldLocation().getWorldName());
        preparedStatement.setInt(2, itemStackDrop.getCurrentWorldLocation().getX());
        preparedStatement.setInt(3, itemStackDrop.getCurrentWorldLocation().getY());
        preparedStatement.setShort(4, itemStackDrop.getCurrentWorldLocation().getZ());
        preparedStatement.setInt(5, itemStackDrop.getItemStack().getItemId());
        preparedStatement.setInt(6, itemStackDrop.getItemStack().getAmount());
        preparedStatement.setInt(7, itemStackDrop.getRespawnTimeMin());
        preparedStatement.setInt(8, itemStackDrop.getRespawnTimeMax());

        return preparedStatement;
    }

    private SqlSearchData searchForData(ItemStackDrop itemStackDrop) {
        return new SqlSearchData("game_world_itemstack_drop", "drop_id", itemStackDrop.getDatabaseId());
    }

    public List<Integer> searchItemStackDrop(String worldName) {

        List<Integer> gameWorldNpcIds = new ArrayList<>();
        String query = "SELECT drop_id FROM game_world_itemstack_drop WHERE world_name=?";

        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, worldName);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int dropId = resultSet.getInt("drop_id");

                gameWorldNpcIds.add(dropId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return gameWorldNpcIds;
    }

    public void firstTimeSaveSQL(ItemStackDrop itemStackDrop) {
        PreparedStatement preparedStatement = null;
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            preparedStatement = firstTimeSave(itemStackDrop, connection);
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
            ServerMain.getInstance().getGameManager().getGameWorldProcessor().loadItemStackDrop(itemStackDrop.getGameWorld());
        }
    }

    public void loadSQL(ItemStackDrop itemStackDrop) {

        ResultSet resultSet = null;
        PreparedStatement searchStatement = null;
        PreparedStatement firstTimeSaveStatement = null;
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {

            SqlSearchData sqlSearchData = searchForData(itemStackDrop);

            searchStatement = connection.prepareStatement("SELECT * FROM " + sqlSearchData.getTableName() + " WHERE " + sqlSearchData.getColumnName() + "=?");
            searchStatement.setObject(1, sqlSearchData.getSetData());
            resultSet = searchStatement.executeQuery();

            if (!resultSet.next()) {
                firstTimeSaveStatement = firstTimeSave(itemStackDrop, connection);
                firstTimeSaveStatement.execute();
            } else {
                databaseLoad(itemStackDrop, resultSet);
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

    public void saveSQL(ItemStackDrop itemStackDrop) {
        PreparedStatement preparedStatement = null;
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            preparedStatement = databaseSave(itemStackDrop, connection);
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

    public void deleteSQL(ItemStackDrop itemStackDrop) {
        PreparedStatement preparedStatement = null;
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            preparedStatement = connection.prepareStatement("DELETE FROM game_world_itemstack_drop WHERE drop_id=?");
            preparedStatement.setInt(1, itemStackDrop.getDatabaseId());
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
