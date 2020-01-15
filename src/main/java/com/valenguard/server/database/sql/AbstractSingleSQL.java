package com.valenguard.server.database.sql;

import com.valenguard.server.Server;
import com.valenguard.server.database.CharacterSaveProgressType;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.ClientHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractSingleSQL implements AbstractSQL {

    abstract void databaseLoad(Player player, ResultSet resultSet) throws SQLException;

    abstract PreparedStatement databaseSave(Player player, Connection connection) throws SQLException;

    abstract PreparedStatement firstTimeSave(Player player, Connection connection) throws SQLException;

    abstract SqlSearchData searchForData(Player player);

    @Override
    public void loadSQL(ClientHandler clientHandler) {
        Player player = clientHandler.getPlayer();

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
    public void saveSQL(ClientHandler clientHandler, CharacterSaveProgressType saveProgressType) {
        Player player = clientHandler.getPlayer();
        PreparedStatement preparedStatement = null;
        boolean saved = false;
        try (Connection connection = Server.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            preparedStatement = databaseSave(player, connection);
            int sqlSave = preparedStatement.executeUpdate();
            if (sqlSave > 0) saved = true;
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
            if (saved) clientHandler.getCharacterSaveProgress().saveProgress(saveProgressType);
        }
    }
}
