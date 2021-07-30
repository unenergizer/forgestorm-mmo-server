package com.forgestorm.server.network;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.ManagerStart;
import com.forgestorm.server.io.todo.NetworkSettingsLoader;
import com.forgestorm.server.network.game.GameServerConnection;
import com.forgestorm.server.network.game.packet.in.*;
import com.forgestorm.server.network.game.packet.out.OutputStreamManager;
import com.forgestorm.server.network.login.LoginServerConnection;
import com.forgestorm.server.util.Log;
import lombok.Getter;

import static com.forgestorm.server.util.Log.println;

@Getter
public class NetworkManager implements ManagerStart {

    private final AuthenticationManager authenticationManager = new AuthenticationManager();
    private OutputStreamManager outStreamManager;
    private LoginServerConnection loginServerConnection;
    private GameServerConnection gameServerConnection;

    @Override
    public void start() {
        Log.println(getClass(), "Initializing network...");

        ServerMain.getInstance().getFileManager().loadNetworkSettingsData();
        NetworkSettingsLoader.NetworkSettingsData networkSettings = ServerMain.getInstance().getFileManager().getNetworkSettingsData();

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
            eventBus.registerListener(new CharacterDeletePacketIn());
            eventBus.registerListener(new CharacterLogoutPacketIn());
            eventBus.registerListener(new CharacterSelectPacketIn());
            eventBus.registerListener(new BankManagePacketIn());
            eventBus.registerListener(new AbilityRequestPacketIn());
            eventBus.registerListener(new AdminEditorEntityPacketIn());
            eventBus.registerListener(new InspectPlayerPacketIn());
            eventBus.registerListener(new ProfileRequestPacketIn());
            eventBus.registerListener(new NPCDialoguePacketIn());
            eventBus.registerListener(new WorldBuilderPacketIn());
            eventBus.registerListener(new TileWarpPacketIn());
            eventBus.registerListener(new DoorInteractPacketIn());
        });

        outStreamManager = new OutputStreamManager();
    }

    public void exit() {
        println(getClass(), "Stopping...");
        loginServerConnection.close();
        gameServerConnection.close();
    }
}
