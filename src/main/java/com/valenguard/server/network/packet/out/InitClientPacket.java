package com.valenguard.server.network.packet.out;

import com.valenguard.server.entity.Player;
import com.valenguard.server.maps.data.Location;
import com.valenguard.server.network.shared.Opcodes;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class InitClientPacket extends ServerOutPacket {

    private boolean loginSuccess;
    private short clientPlayerId;
    private Location location;
    private float moveSpeed;

    public InitClientPacket(Player player, boolean loginSuccess, short clientPlayerId, Location location) {
        super(Opcodes.INIT_PLAYER_CLIENT, player);
        this.loginSuccess = loginSuccess;
        this.clientPlayerId = clientPlayerId;
        this.location = location;
    }

    @Override
    protected void createPacket(ObjectOutputStream write) throws IOException {
        write.writeBoolean(loginSuccess);
        write.writeShort(clientPlayerId);
        write.writeUTF(location.getMapName());

        /*System.out.println("[PACKET] loginSuccess: " + loginSuccess
                + " , EntityID: " + clientPlayerId
                + " , Map: " + location.getMapName());*/
    }
}
