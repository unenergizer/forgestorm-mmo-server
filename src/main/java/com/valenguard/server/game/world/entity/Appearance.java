package com.valenguard.server.game.world.entity;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackType;
import com.valenguard.server.game.world.item.WearableItemStack;
import com.valenguard.server.network.game.packet.out.EntityAppearancePacketOut;
import com.valenguard.server.util.ColorList;
import com.valenguard.server.util.libgdx.Color;
import lombok.Getter;
import lombok.Setter;

import static com.valenguard.server.util.Log.println;

@Getter
@Setter
public class Appearance {

    private byte monsterBodyTexture = -1;
    private byte hairTexture = 0;
    private byte helmTexture = -1;
    private byte chestTexture = -1;
    private byte pantsTexture = -1;
    private byte shoesTexture = -1;
    private int hairColor = Color.rgba8888(ColorList.CORAL.getColor());
    private int eyeColor = Color.rgba8888(ColorList.ROYAL.getColor());
    private int skinColor = Color.rgba8888(ColorList.PLAYER_DEFAULT.getColor());
    private int glovesColor = Color.rgba8888(ColorList.CLEAR.getColor());

    private static final byte REMOVE_TEXTURE = -1;

    private final Entity appearanceOwner;

    public Appearance(Entity appearanceOwner) {
        this.appearanceOwner = appearanceOwner;
    }


    public void updatePlayerAppearance(ItemStack itemStack, ItemStackType itemStackType, boolean sendPacket) {
        println(getClass(), "SendPacket: " + sendPacket);
        if (itemStack != null) {
            println(getClass(), "Equipping non null item and updating appearance!");
            println(getClass(), "ItemStack: " + itemStack.toString());

            if (itemStack instanceof WearableItemStack) {
                setPlayerBody(itemStackType.getAppearanceType(), ((WearableItemStack) itemStack).getTextureId(), sendPacket);
            }
        } else {
            println(getClass(), "Unequipping due to null item so the appearance becomes black!");

            if (itemStackType.getAppearanceType() == null) return;
            setPlayerBody(itemStackType.getAppearanceType(), REMOVE_TEXTURE, sendPacket);
        }
    }

    private void setPlayerBody(AppearanceType appearanceType, byte updateId, boolean sendPacket) {
        switch (appearanceType) {
            case MONSTER_BODY_TEXTURE:
                monsterBodyTexture = updateId;
                break;
            case HAIR_TEXTURE:
                hairTexture = updateId;
                break;
            case HELM_TEXTURE:
                helmTexture = updateId;
                break;
            case CHEST_TEXTURE:
                chestTexture = updateId;
                break;
            case PANTS_TEXTURE:
                pantsTexture = updateId;
                break;
            case SHOES_TEXTURE:
                shoesTexture = updateId;
                break;
            case HAIR_COLOR:
//                hairColor = updateId;
                break;
            case EYE_COLOR:
//                eyeColor = updateId;
                break;
            case SKIN_COLOR:
//                skinColor = updateId;
                break;
            case GLOVES_COLOR:
//                glovesColor = updateId;
                break;
        }
        if (sendPacket) {
            Server.getInstance().getGameManager().sendToAllButPlayer((Player) appearanceOwner, clientHandler ->
                    new EntityAppearancePacketOut(clientHandler.getPlayer(), appearanceOwner));
        }
    }
}
