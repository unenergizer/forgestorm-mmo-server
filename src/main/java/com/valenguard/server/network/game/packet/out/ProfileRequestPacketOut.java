package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;
import com.valenguard.server.profile.XenforoProfile;
import com.valenguard.server.util.Log;

public class ProfileRequestPacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;

    private final XenforoProfile xenforoProfile;

    public ProfileRequestPacketOut(final Player player, final XenforoProfile xenforoProfile) {
        super(Opcodes.PROFILE_REQUEST, player.getClientHandler());
        this.xenforoProfile = xenforoProfile;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        Log.println(getClass(), xenforoProfile.toString(), false, PRINT_DEBUG);

        write.writeString(xenforoProfile.getAccountName());
        write.writeInt(xenforoProfile.getXenforoDatabaseID());
        write.writeInt(xenforoProfile.getMessageCount());
        write.writeInt(xenforoProfile.getTrophyPoints());
        write.writeString(xenforoProfile.getGravatarHash());
        write.writeInt(xenforoProfile.getReactionScore());
    }
}
