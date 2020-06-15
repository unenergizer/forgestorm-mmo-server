package com.valenguard.server.profile;

import com.valenguard.server.util.MD5Util;
import lombok.Getter;

@Getter
public class XenforoProfile {

    private final String accountName;
    private final int xenforoDatabaseID;
    private final int messageCount;
    private final int trophyPoints;
    private final String gravatarEmail;
    private final String gravatarHash;
    private final int reactionScore;

    public XenforoProfile(String accountName, int xenforoDatabaseID, int messageCount, int trophyPoints, String gravatarEmail, int reactionScore) {
        this.accountName = accountName;
        this.xenforoDatabaseID = xenforoDatabaseID;
        this.messageCount = messageCount;
        this.trophyPoints = trophyPoints;
        this.gravatarEmail = gravatarEmail;
        this.reactionScore = reactionScore;

        // Convert gravatar email string into hash
        if (!gravatarEmail.isEmpty()) {
            this.gravatarHash = MD5Util.hashUserEmail(gravatarEmail);
        } else {
            this.gravatarHash = "";
        }
    }

    @Override
    public String toString() {
        return "AccountName: " + accountName
                + ", XenforoDatabaseID: " + xenforoDatabaseID
                + ", MessageCount: " + messageCount
                + ", TrophyPoints: " + trophyPoints
                + ", GravatarEmail: " + gravatarEmail
                + ", GravatarHash: " + gravatarHash
                + ", ReactionScore: " + reactionScore;
    }
}
