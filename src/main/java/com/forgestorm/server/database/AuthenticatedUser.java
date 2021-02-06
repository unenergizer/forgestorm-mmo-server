package com.forgestorm.server.database;

import com.forgestorm.server.profile.SecondaryUserGroups;
import lombok.Getter;

import java.util.List;

@Getter
public class AuthenticatedUser {

    private final String ip;
    private final int databaseUserId;
    private final String xfAccountName;
    private final List<Byte> secondaryGroupIds;
    private final boolean isAdmin;
    private final boolean isModerator;

    public AuthenticatedUser(final String ip, final int databaseUserId, final String xfAccountName, final List<Byte> secondaryGroupIds, final boolean isAdmin, final boolean isModerator) {
        this.ip = ip;
        this.databaseUserId = databaseUserId;
        this.xfAccountName = xfAccountName;
        this.secondaryGroupIds = secondaryGroupIds;
        this.isAdmin = isAdmin;
        this.isModerator = isModerator;
    }

    public boolean isContentDeveloper() {
        return secondaryGroupIds.contains(SecondaryUserGroups.CONTENT_DEVELOPER.getUserGroupId());
    }

}
