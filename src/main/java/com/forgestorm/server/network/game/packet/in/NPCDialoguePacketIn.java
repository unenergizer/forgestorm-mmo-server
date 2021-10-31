package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.game.world.entity.NPC;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.*;
import com.forgestorm.shared.network.game.Opcode;
import com.forgestorm.shared.network.game.Opcodes;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.NPC_DIALOGUE)
public class NPCDialoguePacketIn implements PacketListener<NPCDialoguePacketIn.NPCDialoguePacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {

        short entityId = clientHandler.readShort();

        return new NPCDialoguePacket(entityId);
    }


    // TODO make sure the NPC exist
    @Override
    public boolean sanitizePacket(NPCDialoguePacket packetData) {
        return true;
    }

    @Override
    public void onEvent(NPCDialoguePacket packetData) {

        Player player = packetData.getClientHandler().getPlayer();

         NPC npc = (NPC) player.getGameWorld().getAiEntityController().getEntity(packetData.entityId);

        System.out.println("Talking to NPC: " + npc);

    }

    @AllArgsConstructor
    class NPCDialoguePacket extends PacketData {
        private short entityId;
    }
}
