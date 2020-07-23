package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Appearance;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.ClientHandler;
import com.forgestorm.server.network.game.shared.Opcodes;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class CharacterMenuLoadPacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;

    public CharacterMenuLoadPacketOut(ClientHandler clientHandler) {
        super(Opcodes.CHARACTERS_MENU_LOAD, clientHandler);
    }

    @Override
    protected void createPacket(GameOutputStream write) {

        // TODO: Remove object reference from createPacket method
        Map<Byte, Player> loadedPlayerList = clientHandler.getLoadedPlayers();

        write.writeByte((byte) loadedPlayerList.size()); // Tell client how many times to loop

        for (Map.Entry<Byte, Player> entrySet : loadedPlayerList.entrySet()) {
            byte index = entrySet.getKey();
            Player player = entrySet.getValue();

            write.writeString(player.getName());
            write.writeByte(index); // index of array (player will send back index to load and play character)

            Appearance appearance = player.getAppearance();
            write.writeByte(appearance.getHairTexture());
            write.writeByte(appearance.getHelmTexture());
            write.writeByte(appearance.getChestTexture());
            write.writeByte(appearance.getPantsTexture());
            write.writeByte(appearance.getShoesTexture());
            write.writeInt(appearance.getHairColor());
            write.writeInt(appearance.getEyeColor());
            write.writeInt(appearance.getSkinColor());
            write.writeInt(appearance.getGlovesColor());
            write.writeByte(appearance.getLeftHandTexture());
            write.writeByte(appearance.getRightHandTexture());

            println(PRINT_DEBUG);
            println(getClass(), "------------------------------------------", false, PRINT_DEBUG);
            println(getClass(), "Name: " + player.getName(), false, PRINT_DEBUG);
            println(getClass(), "CharacterIndex: " + index, false, PRINT_DEBUG);
            println(getClass(), "HairTexture: " + appearance.getHairTexture(), false, PRINT_DEBUG);
            println(getClass(), "HelmTexture: " + appearance.getHelmTexture(), false, PRINT_DEBUG);
            println(getClass(), "ChestTexture: " + appearance.getChestTexture(), false, PRINT_DEBUG);
            println(getClass(), "PantsTexture: " + appearance.getPantsTexture(), false, PRINT_DEBUG);
            println(getClass(), "ShoesTexture: " + appearance.getShoesTexture(), false, PRINT_DEBUG);
            println(getClass(), "HairColor: " + appearance.getHairColor(), false, PRINT_DEBUG);
            println(getClass(), "EyeColor: " + appearance.getEyeColor(), false, PRINT_DEBUG);
            println(getClass(), "SkinColor: " + appearance.getSkinColor(), false, PRINT_DEBUG);
            println(getClass(), "GlovesColor: " + appearance.getGlovesColor(), false, PRINT_DEBUG);
            println(getClass(), "LeftHandTexture: " + appearance.getLeftHandTexture(), false, PRINT_DEBUG);
            println(getClass(), "RightHandTexture: " + appearance.getRightHandTexture(), false, PRINT_DEBUG);
        }
    }
}
