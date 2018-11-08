package com.valenguard.server.network;

import com.valenguard.server.network.shared.ClientHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerSessionData {
    private short serverID;
    private Credentials credentials;
    private ClientHandler clientHandler;
}
