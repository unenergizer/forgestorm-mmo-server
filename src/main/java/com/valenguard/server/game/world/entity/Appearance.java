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
    public static final int ARMOR = 2; // Cover Base ID
    public static final int HELM = 3; // Cover Base ID

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
                if (itemStackType == ItemStackType.CHEST) {
                    println(getClass(), "Updating chest");
                    setPlayerArmor(((WearableItemStack) itemStack).getTextureId(), sendPacket);
                } else if (itemStackType == ItemStackType.HELM) {
                    println(getClass(), "Updating helm");
                    setPlayerHelm(((WearableItemStack) itemStack).getTextureId(), sendPacket);
                }
            }
        } else {
            println(getClass(), "Unequipping due to null item so the appearance becomes black!");
            if (itemStackType == ItemStackType.CHEST) {
                setPlayerArmor(REMOVE_TEXTURE, sendPacket);
            } else if (itemStackType == ItemStackType.HELM) {
                setPlayerHelm(REMOVE_TEXTURE, sendPacket);
            }
        }
    }

    private void setPlayerHelm(short helmTextureId, boolean sendPacket) {
        textureIds[Appearance.HELM] = helmTextureId;
        if (sendPacket) {
            Server.getInstance().getGameManager().sendToAllButPlayer((Player) appearanceOwner, clientHandler ->
                    new EntityAppearancePacketOut(clientHandler.getPlayer(), appearanceOwner, EntityAppearancePacketOut.HELM_INDEX).sendPacket());
        }
    }

    private void setPlayerArmor(short armorTextureId, boolean sendPacket) {
        textureIds[Appearance.ARMOR] = armorTextureId;
        if (sendPacket) {
            Server.getInstance().getGameManager().sendToAllButPlayer((Player) appearanceOwner, clientHandler ->
                    new EntityAppearancePacketOut(clientHandler.getPlayer(), appearanceOwner, EntityAppearancePacketOut.ARMOR_INDEX).sendPacket());
        }
    }
}
