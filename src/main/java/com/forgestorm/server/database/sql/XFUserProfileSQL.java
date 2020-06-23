package com.forgestorm.server.database.sql;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.database.AuthenticatedUser;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.profile.XenforoProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.forgestorm.server.util.Log.println;

public class XFUserProfileSQL {

    private static final boolean PRINT_DEBUG = false;

    private XenforoProfile databaseLoad(Player player, ResultSet resultSet) throws SQLException {
        int messageCount = resultSet.getInt("message_count");
        int trophyPoints = resultSet.getInt("trophy_points");
        String gravatar = resultSet.getString("gravatar");
        int reactionScore = resultSet.getInt("reaction_score");

        AuthenticatedUser authenticatedUser = player.getClientHandler().getAuthenticatedUser();

        return new XenforoProfile(authenticatedUser.getXfAccountName(), authenticatedUser.getDatabaseUserId(), messageCount, trophyPoints, gravatar, reactionScore);
    }

    public XenforoProfile loadSQL(Player player) {
        ResultSet resultSet = null;
        PreparedStatement searchStatement = null;
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {

            searchStatement = connection.prepareStatement("SELECT * FROM xf_user WHERE user_id=?");
            searchStatement.setObject(1, player.getClientHandler().getAuthenticatedUser().getDatabaseUserId());
            resultSet = searchStatement.executeQuery();

            XenforoProfile xenforoProfile = null;
            if (!resultSet.next()) {
                println(getClass(), "Problem getting player profile!", false, PRINT_DEBUG);
            } else {
                println(getClass(), "Result found, loading data!", false, PRINT_DEBUG);
                xenforoProfile = databaseLoad(player, resultSet);
            }

            return xenforoProfile;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (searchStatement != null) searchStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
