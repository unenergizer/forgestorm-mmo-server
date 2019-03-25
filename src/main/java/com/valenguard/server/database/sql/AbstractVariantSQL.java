package com.valenguard.server.database.sql;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Player;

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
    public void loadSQL(Player player) {

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
    public void saveSQL(Player player) {

        PreparedStatement[] preparedStatements = null;
        try (Connection connection = Server.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {

            preparedStatements = databaseSave(player, connection);
            for (PreparedStatement preparedStatement : preparedStatements) {
                preparedStatement.execute();
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
        }
    }
}
