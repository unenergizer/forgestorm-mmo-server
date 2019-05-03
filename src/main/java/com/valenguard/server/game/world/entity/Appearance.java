package com.valenguard.server.game.world.entity;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackType;
import com.valenguard.server.game.world.item.WearableItemStack;
import com.valenguard.server.network.game.packet.out.EntityAppearancePacketOut;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import static com.valenguard.server.util.Log.println;

@AllArgsConstructor
public class Appearance {

    public static final int BODY = 0; // Base ID
    public static final int HEAD = 1; // Base ID
    public static final int HELM = 2;
    public static final int CHEST = 3;
    public static final int PANTS = 4;
    public static final int SHOES = 5;

    private static final short REMOVE_TEXTURE = -1;

    private final Entity appearanceOwner;

    @Setter
    @Getter
    private byte colorId;

    /**
     * IDs are arranged from head to toe or from top to bottom.
     */
    @Setter
    @Getter
    private short[] textureIds;

    public short getTextureId(int index) {
        return textureIds[index];
    }

    public void updatePlayerAppearance(ItemStack itemStack, ItemStackType itemStackType, boolean sendPacket) {
        println(getClass(), "SendPacket: " + sendPacket);
        if (itemStack != null) {
            println(getClass(), "Equipping non null item and updating appearance!");
            println(getClass(), "ItemStack: " + itemStack.toString());

            if (itemStack instanceof WearableItemStack) {
                setPlayerBody(itemStackType.getAppearanceId(), ((WearableItemStack) itemStack).getTextureId(), sendPacket);
            }
        } else {
            println(getClass(), "Unequipping due to null item so the appearance becomes black!");

            if (itemStackType.getAppearanceId() == 0) return;
            setPlayerBody(itemStackType.getAppearanceId(), REMOVE_TEXTURE, sendPacket);

        }
    }

    private void setPlayerBody(int appearanceId, short textureId, boolean sendPacket) {
        textureIds[appearanceId] = textureId;
        byte changeBit = 0x00;
        switch (appearanceId) {
            case HELM:
                changeBit = EntityAppearancePacketOut.HELM_INDEX;
                break;
            case CHEST:
                changeBit = EntityAppearancePacketOut.CHEST_INDEX;
                break;
            case PANTS:
                changeBit = EntityAppearancePacketOut.PANTS_INDEX;
                break;
            case SHOES:
                changeBit = EntityAppearancePacketOut.SHOES_INDEX;
                break;
        }

        final byte appearanceByte = changeBit;

        if (sendPacket) {
            Server.getInstance().getGameManager().sendToAllButPlayer((Player) appearanceOwner, clientHandler ->
                    new EntityAppearancePacketOut(clientHandler.getPlayer(), appearanceOwner, appearanceByte));
        }
    }
}
