package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.EntityType;
import com.valenguard.server.game.world.entity.MovingEntity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.ABILITY_REQUEST)
public class AbilityRequestPacketIn implements PacketListener<AbilityRequestPacketIn.AbilityRequestPacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {

        final short abilityId = clientHandler.readShort();
        final byte targetEntityType = clientHandler.readByte();
        final short targetId = clientHandler.readShort();

        return new AbilityRequestPacket(abilityId, EntityType.getEntityType(targetEntityType), targetId);
    }

    @Override
    public boolean sanitizePacket(AbilityRequestPacket packetData) {
        // TODO: sanitize...
        return true;
    }

    @Override
    public void onEvent(AbilityRequestPacket packetData) {

        Player attacker = packetData.getClientHandler().getPlayer();
        MovingEntity movingEntity = null;

        switch (packetData.entityType) {
            case CLIENT_PLAYER:
            case PLAYER:
                movingEntity = attacker.getGameMap().getPlayerController().findPlayer(packetData.targetId);
                break;
            case NPC:
            case MONSTER:
                movingEntity = (MovingEntity) attacker.getGameMap().getAiEntityController().getEntity(packetData.targetId);
                break;
        }

        if (movingEntity == null) {
            return;
        }

        Server.getInstance().getAbilityManager().performPlayerAbility(
                packetData.abilityId,
                attacker,
                movingEntity);
    }

    @AllArgsConstructor
    class AbilityRequestPacket extends PacketData {
        private short abilityId;
        private EntityType entityType;
        private short targetId;
    }
}
