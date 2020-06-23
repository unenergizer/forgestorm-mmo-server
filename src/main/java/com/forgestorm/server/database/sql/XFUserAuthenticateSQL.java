package com.forgestorm.server.database.sql;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.forgestorm.server.ServerMain;
import com.forgestorm.server.network.login.LoginState;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import static com.forgestorm.server.util.Log.println;

public class XFUserAuthenticateSQL {

    private static final String GET_USER_ID = "SELECT user_id, username, is_moderator, is_admin FROM xf_user WHERE username=?";
    private static final String GET_USER_HASH = "SELECT data FROM xf_user_authenticate WHERE user_id=?";

    private XFUserAuthenticateSQL() {
    }

    public static LoginState authenticate(String xfAccountName, String xfPassword) {
        ResultSet userIdResult = null;
        ResultSet hashResult = null;
        PreparedStatement getUserIdStatement = null;
        PreparedStatement getHashStatement = null;
        int databaseUserId;
        String databaseUsername;
        boolean isAdmin;
        boolean isModerator;

        if (ServerMain.getInstance().getNetworkManager().getOutStreamManager().isAccountOnline(xfAccountName)) {
            return new LoginState().failState("User already logged in.");
        }

        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {

            getUserIdStatement = connection.prepareStatement(GET_USER_ID);
            getUserIdStatement.setString(1, xfAccountName);

            userIdResult = getUserIdStatement.executeQuery();

            if (userIdResult.next()) {

                databaseUserId = userIdResult.getInt("user_id");
                databaseUsername = userIdResult.getString("username");
                isModerator = userIdResult.getBoolean("is_moderator");
                isAdmin = userIdResult.getBoolean("is_admin");

                getHashStatement = connection.prepareStatement(GET_USER_HASH);
                getHashStatement.setInt(1, databaseUserId);

                hashResult = getHashStatement.executeQuery();

                if (hashResult.next()) {

                    Blob hashBlob = hashResult.getBlob("data");
                    InputStream inputStream = hashBlob.getBinaryStream();

                    int ch;

                    //read bytes from ByteArrayInputStream using read method
                    StringBuilder stored = new StringBuilder();
                    while ((ch = inputStream.read()) != -1) {
                        stored.append((char) ch);
                    }
                    String storedStr = stored.toString();
                    String hash = storedStr.substring(22, storedStr.length() - 3);

                    BCrypt.Result result = BCrypt.verifyer().verify(xfPassword.toCharArray(), hash);
                    if (!result.verified) {
                        return new LoginState().failState("Incorrect password");
                    }

                } else {
                    println(XFUserAuthenticateSQL.class, "The user Id existed in the xf_user table but not in the xf_user_authenticate table.", true);
                    return new LoginState().failState("Incorrect username");
                }

            } else {
                return new LoginState().failState("Incorrect username");
            }


        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return new LoginState().failState("Server failed to connect");
        } finally {

            try {
                if (userIdResult != null) userIdResult.close();
                if (hashResult != null) hashResult.close();
                if (getUserIdStatement != null) getUserIdStatement.close();
                if (getHashStatement != null) getHashStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return new LoginState().successState(databaseUserId, databaseUsername, isAdmin, isModerator);
    }
}
