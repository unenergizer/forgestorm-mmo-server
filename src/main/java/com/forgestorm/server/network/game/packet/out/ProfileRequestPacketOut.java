package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.Opcodes;
import com.forgestorm.server.profile.XenforoProfile;
import com.forgestorm.server.util.Log;

public class ProfileRequestPacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;

    private final String accountName;
    private final int xenforoDatabaseID;
    private final int messageCount;
    private final int trophyPoints;
    private final String gravatarHash;
    private final int reactionScore;

    public ProfileRequestPacketOut(final Player player, final XenforoProfile xenforoProfile) {
        super(Opcodes.PROFILE_REQUEST, player.getClientHandler());

        this.accountName = xenforoProfile.getAccountName();
        this.xenforoDatabaseID = xenforoProfile.getXenforoDatabaseID();
        this.messageCount = xenforoProfile.getMessageCount();
        this.trophyPoints = xenforoProfile.getTrophyPoints();
        this.gravatarHash = xenforoProfile.getGravatarHash();
        this.reactionScore = xenforoProfile.getReactionScore();

        Log.println(getClass(), xenforoProfile.toString(), false, PRINT_DEBUG);
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeString(accountName);
        write.writeInt(xenforoDatabaseID);
        write.writeInt(messageCount);
        write.writeInt(trophyPoints);
        write.writeString(gravatarHash);
        write.writeInt(reactionScore);
    }
}
