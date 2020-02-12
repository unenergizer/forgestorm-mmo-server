package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.packet.out.ChatMessagePacketOut;
import com.valenguard.server.network.game.packet.out.InspectPlayerPacketOut;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.valenguard.server.util.Log.println;

@Opcode(getOpcode = Opcodes.INSPECT_PLAYER)
public class InspectPlayerPacketIn implements PacketListener<InspectPlayerPacketIn.InspectPlayerPacket>, PacketInCancelable {

    private static final boolean PRINT_DEBUG = false;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        short entityId = clientHandler.readShort();
        return new InspectPlayerPacket(entityId);
    }

    @Override
    public boolean sanitizePacket(InspectPlayerPacket packetData) {
        return true;
    }

    @Override
    public void onEvent(InspectPlayerPacket packetData) {
        Player player = Server.getInstance().getGameManager().findPlayer(packetData.entityId);
        if (player != null) {
            new InspectPlayerPacketOut(packetData.getClientHandler().getPlayer(), player).sendPacket();
            println(getClass(), "Sending inspection data!", false, PRINT_DEBUG);
        } else {
            new ChatMessagePacketOut(packetData.getClientHandler().getPlayer(), "[RED]Could not find player.").sendPacket();
            println(getClass(), "NOT sending inspection data!", false, PRINT_DEBUG);
        }
    }

    @Override
    public List<Class<? extends PacketListener>> excludeCanceling() {
        List<Class<? extends PacketListener>> excludeCanceling = new ArrayList<>();
        excludeCanceling.add(ChatMessagePacketIn.class);
        excludeCanceling.add(PingPacketIn.class);
        return excludeCanceling;
    }

    @Override
    public void onCancel(Player player) {
        player.setCurrentShoppingEntity(null);
    }

    @AllArgsConstructor
    class InspectPlayerPacket extends PacketData {
        private short entityId;
    }
}
