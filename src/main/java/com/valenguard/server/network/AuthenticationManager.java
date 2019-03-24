package com.valenguard.server.network;

import com.valenguard.server.game.mysql.AuthenticatedUser;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.valenguard.server.util.Log.println;

public class AuthenticationManager {

    // TODO: Create an automatic timeout remove method. If it takes X amount of time to make the switch, kick them

    private final Map<UUID, AuthenticatedUser> authenticatedUserMap = new ConcurrentHashMap<>();

    public void addLoginUser(UUID uuid, AuthenticatedUser authenticatedUser) {
        println(getClass(), "User authenticated! Adding them to the map!");
        authenticatedUserMap.put(uuid, authenticatedUser);
    }

    public boolean authGameUser(UUID uuid, String ipAddress) {
        if (!authenticatedUserMap.containsKey(uuid)) return false;
        return authenticatedUserMap.get(uuid).getIp().equals(ipAddress);
    }

    public void removeEntry(UUID uuid) {
        authenticatedUserMap.remove(uuid);
    }

    public int getDatabaseUserId(UUID uuid) {
        return authenticatedUserMap.get(uuid).getDatabaseUserId();
    }

    public String getUsername(UUID uuid) {
        return authenticatedUserMap.get(uuid).getUsername();
    }
}
