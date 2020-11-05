package com.forgestorm.server.game.world.tile.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TilePropertyTypes {
    COLLISION_BLOCK(new CollisionBlockProperty()),
    DOOR(new DoorProperty()),
    INTERACTIVE_CONTAINER(new ContainerProperty()),
    WANG_TILE(new WangTileProperty()),
    BLOCK_MOVE_DIRECTION(new BlockMoveDirectionProperty()),
    JUMP_TO_DIRECTION(new JumpToDirectionProperty()),
    LADDER(new LadderProperty()),
    WATER(new WaterProperty()),
    INTERACT_DAMAGE(new InteractDamageProperty()),
    WALK_OVER_SOUND(new TileWalkOverSoundProperty());

    private AbstractTileProperty abstractTileProperty;

    @Override
    public String toString() {
        String name = name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
