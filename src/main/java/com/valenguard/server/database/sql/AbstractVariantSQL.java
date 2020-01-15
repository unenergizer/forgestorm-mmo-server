package com.valenguard.server.database.sql;

import com.valenguard.server.Server;
import com.valenguard.server.database.CharacterSaveProgressType;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.ClientHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractVariantSQL<T> implements AbstractSQL {

    abstract void databaseLoad(Player player, ResultSet resultSet) throws SQLException;

    abstract PreparedStatement firstTimeSave(Player player, Connection connection, T index) throws SQLException;

    abstract PreparedStatement[] databaseSave(Player player, Connection connection) throws SQLException;

    abstract SqlSearchData searchForData(Player player);

    abstract VariantData<T> getVariantData(Player player);

    @Override
    public void loadSQL(ClientHandler clientHandler) {
        Player player = clientHandler.getPlayer();

        VariantData<T> variantData = getVariantData(player);
        PreparedStatement searchStatement = null;
        ResultSet resultSet = null;
        try (Connection connection = Server.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {

            SqlSearchData sqlSearchData = searchForData(player);

            searchStatement = connection.prepareStatement("SELECT * FROM " + sqlSearchData.getTableName() + " WHERE "
                    + sqlSearchData.getColumnName() + "=?");

            searchStatement.setObject(1, sqlSearchData.getSetData());

            resultSet = searchStatement.executeQuery();

            boolean[] foundData = new boolean[variantData.getSearchObjects().size()];
            while (resultSet.next()) {
                Object searchIndex = resultSet.getObject(variantData.getSearchString());

                databaseLoad(player, resultSet);

                // Since the data exist for the result set let's find it's associated data index.
                for (int compIndex = 0; compIndex < variantData.getSearchObjects().size(); compIndex++) {
                    if (searchIndex.equals(variantData.getSearchObjects().get(compIndex))) {
                        foundData[compIndex] = true;
                        break;
                    }
                }
            }

            for (int i = 0; i < foundData.length; i++) {
                if (!foundData[i]) {
                    PreparedStatement preparedStatement = firstTimeSave(player, connection, variantData.getSearchObjects().get(i));
                    preparedStatement.execute();
                    preparedStatement.close(); // TODO extract this into the finally block
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (searchStatement != null) searchStatement.close();
                if (resultSet != null) resultSet.close();
                if (searchStatement != null) searchStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveSQL(ClientHandler clientHandler, CharacterSaveProgressType saveProgressType) {
        Player player = clientHandler.getPlayer();
        PreparedStatement[] preparedStatements = null;

        // Track to make sure everything was saved
        Boolean[] progressSaved = new Boolean[0];

        try (Connection connection = Server.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {

            preparedStatements = databaseSave(player, connection);
            progressSaved = new Boolean[preparedStatements.length];

            for (int i = 0; i < preparedStatements.length; i++) {

                // Run prepared statement and save execution progress
                int sqlSave = preparedStatements[i].executeUpdate();
                progressSaved[i] = sqlSave > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatements != null) {
                    for (PreparedStatement preparedStatement : preparedStatements) {
                        preparedStatement.close();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Set to true automatically. If anything returns false, then we set to false.
            boolean allSaved = true;

            // Test to see if we have any false booleans (unsaved data)
            for (Boolean b : progressSaved) {
                if (!b) {
                    allSaved = false;
                    break;
                }
            }

            // Finally if everything was saved, then we do the following...
            if (allSaved) clientHandler.getCharacterSaveProgress().saveProgress(saveProgressType);
        }
    }
}
