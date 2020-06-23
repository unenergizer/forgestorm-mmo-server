package com.forgestorm.server.network;

import com.forgestorm.server.database.AuthenticatedUser;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthenticationManager {

    // TODO: Create an automatic timeout remove method. If it takes X amount of time to make the switch, kick them

    private final Map<UUID, AuthenticatedUser> authenticatedUserMap = new ConcurrentHashMap<>();

    public void addLoginUser(UUID uuid, AuthenticatedUser authenticatedUser) {
        authenticatedUserMap.put(uuid, authenticatedUser);
    }

    public boolean authGameUser(UUID uuid, String ipAddress) {
        if (!authenticatedUserMap.containsKey(uuid)) return false;
        return authenticatedUserMap.get(uuid).getIp().equals(ipAddress);
    }

    public void removeEntry(UUID uuid) {
        authenticatedUserMap.remove(uuid);
    }

    public AuthenticatedUser getAuthData(UUID uuid) {
        return authenticatedUserMap.get(uuid);
    }

}
