package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.ClientHandler;
import com.valenguard.server.network.game.shared.Opcodes;

import java.util.Map;

public class CharacterMenuLoadPacketOut extends AbstractServerOutPacket {

    public CharacterMenuLoadPacketOut(ClientHandler clientHandler) {
        super(Opcodes.CHARACTERS_MENU_LOAD, clientHandler);
    }

    @Override
    protected void createPacket(GameOutputStream write) {

        Map<Byte, Player> loadedPlayerList = clientHandler.getLoadedPlayers();

        write.writeByte((byte) loadedPlayerList.size()); // Tell client how many times to loop

        for (Map.Entry<Byte, Player> entrySet : loadedPlayerList.entrySet()) {
            byte index = entrySet.getKey();
            Player player = entrySet.getValue();

            write.writeString(player.getName());
            write.writeByte(index); // index of array (player will send back index to load and play character)

            Appearance appearance = player.getAppearance();
            write.writeShort(appearance.getTextureId(Appearance.BODY));
            write.writeShort(appearance.getTextureId(Appearance.HEAD));
            write.writeByte(appearance.getColorId());
        }
    }
}
