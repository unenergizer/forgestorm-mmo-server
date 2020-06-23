package com.forgestorm.server.profile;

import com.forgestorm.server.database.sql.XFUserProfileSQL;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.packet.out.ProfileRequestPacketOut;

import java.util.HashMap;
import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class XenforoProfileManager {

    private static final boolean PRINT_DEBUG = false;

    private final Map<Integer, XenforoProfile> xenforoProfileMap = new HashMap<>();

    private XenforoProfile getXenforoProfile(Player profileToGet) {
        if (xenforoProfileMap.containsKey(profileToGet.getDatabaseId())) {
            println(getClass(), "Profile already loaded. Sending it now.", false, PRINT_DEBUG);
            return xenforoProfileMap.get(profileToGet.getDatabaseId());
        } else {
            println(getClass(), "Requesting profile from database...", false, PRINT_DEBUG);
            println(getClass(), "DatabaseID: " + profileToGet.getDatabaseId(), false, PRINT_DEBUG);

            XenforoProfile xenforoProfile = new XFUserProfileSQL().loadSQL(profileToGet);
            xenforoProfileMap.put(profileToGet.getDatabaseId(), xenforoProfile);
            return xenforoProfile;
        }
    }

    public void sendXenforoProfile(Player profileToGet, Player profileRequester) {
        new ProfileRequestPacketOut(profileRequester, getXenforoProfile(profileToGet)).sendPacket();
    }
}
