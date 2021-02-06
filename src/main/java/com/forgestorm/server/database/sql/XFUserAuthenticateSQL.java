package com.forgestorm.server.database.sql;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.forgestorm.server.ServerMain;
import com.forgestorm.server.network.login.LoginFailReason;
import com.forgestorm.server.network.login.LoginState;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.forgestorm.server.util.Log.println;

public class XFUserAuthenticateSQL {

    private static final String GET_USER_ID = "SELECT user_id, username, secondary_group_ids, is_moderator, is_admin, is_banned FROM xf_user WHERE username=?";
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
        String secondaryGroupIds;
        boolean isModerator;
        boolean isAdmin;
        boolean isBanned;

        if (ServerMain.getInstance().getNetworkManager().getOutStreamManager().isAccountOnline(xfAccountName)) {
            return new LoginState().failState(LoginFailReason.ALREADY_LOGGED_IN);
        }

        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {

            getUserIdStatement = connection.prepareStatement(GET_USER_ID);
            getUserIdStatement.setString(1, xfAccountName);

            userIdResult = getUserIdStatement.executeQuery();

            if (userIdResult.next()) {

                databaseUserId = userIdResult.getInt("user_id");
                databaseUsername = userIdResult.getString("username");
                secondaryGroupIds = userIdResult.getString("secondary_group_ids");
                isModerator = userIdResult.getBoolean("is_moderator");
                isAdmin = userIdResult.getBoolean("is_admin");
                isBanned = userIdResult.getBoolean("is_banned");

                if (isBanned) return new LoginState().failState(LoginFailReason.ACCOUNT_BANNED);

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
                        return new LoginState().failState(LoginFailReason.INCORRECT_ACCOUNT_DETAILS);
                    }

                } else {
                    println(XFUserAuthenticateSQL.class, "The user Id existed in the xf_user table but not in the xf_user_authenticate table.", true);
                    return new LoginState().failState(LoginFailReason.INCORRECT_ACCOUNT_DETAILS);
                }

            } else {
                return new LoginState().failState(LoginFailReason.INCORRECT_ACCOUNT_DETAILS);
            }


        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return new LoginState().failState(LoginFailReason.INCORRECT_ACCOUNT_DETAILS);
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

        // Convert the String of secondary group id's into an Byte list
        String[] array = secondaryGroupIds.split(",", -1);
        List<Byte> byteList = new ArrayList<>();

        for (String s : array) {
            byteList.add(Byte.parseByte(s));
        }

        return new LoginState().successState(
                databaseUserId,
                databaseUsername,
                byteList,
                isAdmin,
                isModerator);
    }
}
