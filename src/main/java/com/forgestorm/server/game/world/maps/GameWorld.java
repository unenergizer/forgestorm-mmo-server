package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.PlayerConstants;
import com.forgestorm.server.game.world.entity.Entity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.tile.TileImage;
import com.forgestorm.server.game.world.tile.properties.TilePropertyTypes;
import com.forgestorm.server.io.todo.ChunkLoader;
import com.forgestorm.server.io.todo.FileManager;
import com.forgestorm.server.util.libgdx.Color;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class GameWorld {

    private final String chunkPath;
    private final String worldName;
    private final int widthInChunks, heightInChunks;
    private final Color backgroundColor;

    private final PlayerController playerController = new PlayerController(this);
    private final AiEntityController aiEntityController = new AiEntityController(this);
    private final StationaryEntityController stationaryEntityController = new StationaryEntityController(this);
    private final ItemStackDropEntityController itemStackDropEntityController = new ItemStackDropEntityController(this);

    //    private Map<Integer, TileImage[]> layers;
    private Map<Integer, WorldChunk> worldChunkMap = new HashMap<>();

    public GameWorld(String chunkPath, String worldName, int widthInChunks, int heightInChunks, Color backgroundColor) {
        this.chunkPath = chunkPath.replace(".json", "/");
        this.worldName = worldName;
        this.widthInChunks = widthInChunks;
        this.heightInChunks = heightInChunks;
        this.backgroundColor = backgroundColor;
    }

    public void loadChunks() {
        FileManager fileManager = ServerMain.getInstance().getFileManager();

        for (short chunkY = 0; chunkY < heightInChunks; chunkY++) {
            for (short chunkX = 0; chunkX < widthInChunks; chunkX++) {
                String chunkPath = this.chunkPath + chunkX + "." + chunkY + ".json";
                fileManager.loadWorldChunkData(chunkPath, true);
                ChunkLoader.WorldChunkDataWrapper wrapper = fileManager.getWorldChunkData(chunkPath);
                if (wrapper == null) continue;
                WorldChunk worldChunk = wrapper.getWorldChunk();
                worldChunkMap.put((chunkX << 16) | (chunkY & 0xFFFF), worldChunk);
            }
        }
    }

    public void calculateVisibleEntities(Player player) {

        // TODO: should we use future location?
        int playerX = player.getFutureWorldLocation().getX();
        int playerY = player.getFutureWorldLocation().getY();

        int clientChunkX = (int) Math.floor(playerX / (float) GameConstants.CHUNK_SIZE);
        int clientChunkY = (int) Math.floor(playerY / (float) GameConstants.CHUNK_SIZE);

        // 1. Check if the NPC is removed. If so remove it from the list!
        //for (Entity visibleEntity : player.getVisibleEntities()) {
        // if (visibleEntity.isDead()) { player.getVisibleEntities().remove(visibleEntity);
        // And send despawn information!
        // }
        // }

        // 2. Collect all entities (within view range) into 1 singular list!
        // Then check if that entity is not within the list and not dead/removed, ect...
        // And spawn


        List<Entity> allVisibleEntities = new ArrayList<>();
        for (int chunkY = clientChunkY - GameConstants.VISIBLE_CHUNK_RADIUS; chunkY < clientChunkY + GameConstants.VISIBLE_CHUNK_RADIUS + 1; chunkY++) {
            for (int chunkX = clientChunkX - GameConstants.VISIBLE_CHUNK_RADIUS; chunkX < clientChunkX + GameConstants.VISIBLE_CHUNK_RADIUS + 1; chunkX++) {
                WorldChunk chunk = findChunk((short) chunkX, (short) chunkY);

                if (chunk == null) continue;


                // List<Entity> entities = chunk.getEntitiesInChunk();
                // allVisibleEntities.addAll(entities);

            }
        }


    }

    public Warp getWarp(int entityX, int entityY) {
        WorldChunk worldChunk = findChunk(entityX, entityY);
        if (worldChunk == null) return null;

        short localX = (short) (entityX - worldChunk.getChunkX() * GameConstants.CHUNK_SIZE);
        short localY = (short) (entityY - worldChunk.getChunkY() * GameConstants.CHUNK_SIZE);

        return worldChunk.getWarp(localX, localY);
    }

    public boolean isTraversable(Location location) {
        return isTraversable(location.getX(), location.getY());
    }

    public boolean isTraversable(int entityX, int entityY) {
        WorldChunk worldChunk = findChunk(entityX, entityY);
        if (worldChunk == null) return false;

        int localX = entityX - worldChunk.getChunkX() * GameConstants.CHUNK_SIZE;
        int localY = entityY - worldChunk.getChunkY() * GameConstants.CHUNK_SIZE;

        TileImage tileImage = worldChunk.getTileImage(LayerDefinition.COLLIDABLES, localX, localY);
        if (tileImage == null) return true;
        return tileImage.containsProperty(TilePropertyTypes.COLLISION_BLOCK);
    }

    public WorldChunk findChunk(int entityX, int entityY) {

        // Convert world coordinates to chunk location
        short chunkX = (short) Math.floor(entityX / (float) GameConstants.CHUNK_SIZE);
        short chunkY = (short) Math.floor(entityY / (float) GameConstants.CHUNK_SIZE);

        return findChunk(chunkX, chunkY);
    }

    WorldChunk findChunk(short chunkX, short chunkY) {
        return worldChunkMap.get((chunkX << 16) | (chunkY & 0xFFFF));
    }

    public Warp getWarpFromLocation(Location location) {
        return getWarp(location.getX(), location.getY());
    }

    public boolean locationHasWarp(Location location) {
        return getWarp(location.getX(), location.getY()) != null;
    }

    public Location getLocation(MoveDirection direction) {
        if (direction == MoveDirection.SOUTH) return new Location(worldName, 0, -1);
        if (direction == MoveDirection.NORTH) return new Location(worldName, 0, 1);
        if (direction == MoveDirection.WEST) return new Location(worldName, -1, 0);
        if (direction == MoveDirection.EAST) return new Location(worldName, 1, 0);
        if (direction == MoveDirection.NONE) return new Location(worldName, 0, 0);
        throw new RuntimeException("Tried to get a location, but direction could not be determined. WorldName: " + worldName + ", MoveDirection: " + direction);
    }

    public boolean isGraveYardWorld() {
        return worldName.equals(PlayerConstants.RESPAWN_LOCATION.getWorldName());
    }
}
