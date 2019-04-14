package com.valenguard.server.network;

import com.valenguard.server.io.NetworkSettingsLoader;
import com.valenguard.server.network.game.GameServerConnection;
import com.valenguard.server.network.game.packet.in.*;
import com.valenguard.server.network.game.packet.out.OutputStreamManager;
import com.valenguard.server.network.login.LoginServerConnection;
import com.valenguard.server.util.Log;
import lombok.Getter;

import static com.valenguard.server.util.Log.println;

@Getter
public class NetworkManager {

    private final AuthenticationManager authenticationManager = new AuthenticationManager();
    private OutputStreamManager outStreamManager;
    private LoginServerConnection loginServerConnection;
    private GameServerConnection gameServerConnection;

    public void start() {
        Log.println(getClass(), "Initializing network...");
        NetworkSettingsLoader.NetworkSettings networkSettings = new NetworkSettingsLoader().loadNetworkSettings();

        loginServerConnection = new LoginServerConnection(this);
        loginServerConnection.openServer(networkSettings);

        gameServerConnection = new GameServerConnection(this);
        gameServerConnection.openServer(networkSettings, (eventBus) -> {
            eventBus.registerListener(new PlayerMovePacketIn());
            eventBus.registerListener(new PingPacketIn());
            eventBus.registerListener(new ChatMessagePacketIn());
            eventBus.registerListener(new PlayerAppearancePacketIn());
            eventBus.registerListener(new InventoryPacketIn());
            eventBus.registerListener(new ClickActionPacketIn());
            eventBus.registerListener(new PlayerTradePacketIn());
            eventBus.registerListener(new ShopPacketIn());
            eventBus.registerListener(new CharacterCreatorPacketIn());
            eventBus.registerListener(new CharacterSelectPacketIn());
        });

        outStreamManager = new OutputStreamManager();
    }

    public void exit() {
        println(getClass(), "Stopping...");
        loginServerConnection.close();
        gameServerConnection.close();
    }
}
