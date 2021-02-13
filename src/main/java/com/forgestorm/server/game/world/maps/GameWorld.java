package com.forgestorm.server.game.world.maps;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.PlayerConstants;
import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.tile.TileImage;
import com.forgestorm.server.io.todo.FileManager;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;
import com.forgestorm.server.util.libgdx.Color;
import lombok.Getter;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static com.forgestorm.server.util.Log.println;
import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public class GameWorld {

    private final String chunkPath;
    private final String worldName;
    private final Color backgroundColor;

    private final PlayerController playerController = new PlayerController(this);
    private final AiEntityController aiEntityController = new AiEntityController(this);
    private final StationaryEntityController stationaryEntityController = new StationaryEntityController(this);
    private final ItemStackDropEntityController itemStackDropEntityController = new ItemStackDropEntityController(this);

    private final Map<Integer, WorldChunk> worldChunkMap = new HashMap<>();

    public GameWorld(String chunkPath, String worldName, Color backgroundColor) {
        this.chunkPath = chunkPath.replace(".json", "/");
        this.worldName = worldName;
        this.backgroundColor = backgroundColor;
    }

    public void loadChunks() {
        FileManager fileManager = ServerMain.getInstance().getFileManager();
        File[] chunkFiles = new File(chunkPath).listFiles((d, name) -> name.endsWith(GameConstants.MAP_FILE_EXTENSION_TYPE));
        checkNotNull(chunkFiles, "No world chunks were found.");

        for (File file : chunkFiles) {
            String path = fileManager.loadWorldChunkData(file, true);
            addChunk(fileManager.getWorldChunkData(path).getWorldChunk());
        }

        println(getClass(), "Loaded " + worldChunkMap.size() + "/" + chunkFiles.length + " chunks for game world \"" + worldName + "\".");
    }

    public void saveChunksOnTick(long ticksPassed) {
        if (ticksPassed % (GameConstants.MAP_SAVE_INTERVAL_IN_MINUTES) == 0) {
            saveChunks(false);
            // Send world save message
            // TODO: Filter players by staff status.
            ServerMain.getInstance().getGameManager().forAllPlayers(player ->
                    new ChatMessagePacketOut(player, ChatChannelType.STAFF, "[GREEN] GameWorld " + worldName + " has been saved.").sendPacket());
        }
    }

    public void saveChunks(boolean manualSave) {
        // Check if saving actually needs to happen
        if (!manualSave) {
            boolean noNewEditsDetected = false;
            for (WorldChunk worldChunk : worldChunkMap.values()) {
                if (worldChunk.isChangedSinceLastSave()) {
                    noNewEditsDetected = true;
                    break;
                }
            }
            if (!noNewEditsDetected) {
                println(getClass(), "NOT SAVING CHUNKS, NO EDITS HAPPENED!");
                return;
            }
            println(getClass(), "Saving chunks for GameWorld " + getWorldName() + ".");
        } else {
            println(getClass(), "Player manually saving chunks for GameWorld " + getWorldName() + ".");
        }

        for (WorldChunk worldChunk : worldChunkMap.values()) {

            // Check if chunk needs to save
            if (!worldChunk.isChangedSinceLastSave()) continue;
            worldChunk.setChangedSinceLastSave(false);

            // Get chunk file
            String chunkPath = this.chunkPath + worldChunk.getChunkX() + "." + worldChunk.getChunkY() + ".json";
            File chunkFile = new File(chunkPath);
            Json json = new Json();
            StringWriter jsonText = new StringWriter();
            JsonWriter writer = new JsonWriter(jsonText);
            json.setOutputType(JsonWriter.OutputType.json);
            json.setWriter(writer);

            // Save Layer
//            json.writeObjectStart("layers");
            json.writeObjectStart();
            for (Map.Entry<LayerDefinition, TileImage[]> entrySet : worldChunk.getLayers().entrySet()) {
                LayerDefinition layerDefinition = entrySet.getKey();
                TileImage[] tileImages = entrySet.getValue();
                StringBuilder ids = new StringBuilder();
                for (TileImage tileImage : tileImages) {
                    if (tileImage == null) {
                        ids.append("0,");
                    } else {
                        ids.append(tileImage.toString());
                    }
                }
                json.writeValue(layerDefinition.getLayerName(), ids.toString());
            }
            json.writeObjectEnd();

            // Save Tile Warp
//            List<Warp> tileWarps = worldChunk.getTileWarps();
//            if (!tileWarps.isEmpty()) {
//                json.writeObjectStart("warps");
//                json.writeValue(tileWarps);
//                json.writeObjectEnd();
//            }

            FileHandle fileHandle = new FileHandle(chunkFile);
            fileHandle.writeString(json.prettyPrint(json.getWriter().getWriter().toString()), false);
        }
    }

    public WorldChunk generateNewChunk(short chunkX, short chunkY) {
        WorldChunk worldChunk = new WorldChunk(chunkX, chunkY, true);
        addChunk(worldChunk);
        println(getClass(), "Generated a new chunk at ChunkX: " + chunkX + ", ChunkY: " + chunkY, true);
        return worldChunk;
    }

    public void addChunk(WorldChunk worldChunk) {
        worldChunkMap.put((worldChunk.getChunkX() << 16) | (worldChunk.getChunkY() & 0xFFFF), worldChunk);
    }

//    public void calculateVisibleEntities(Player player) {
//
//        // TODO: should we use future location?
//        int playerX = player.getFutureWorldLocation().getX();
//        int playerY = player.getFutureWorldLocation().getY();
//
//        int clientChunkX = (int) Math.floor(playerX / (float) GameConstants.CHUNK_SIZE);
//        int clientChunkY = (int) Math.floor(playerY / (float) GameConstants.CHUNK_SIZE);
//
//        // 1. Check if the NPC is removed. If so remove it from the list!
//        //for (Entity visibleEntity : player.getVisibleEntities()) {
//        // if (visibleEntity.isDead()) { player.getVisibleEntities().remove(visibleEntity);
//        // And send despawn information!
//        // }
//        // }
//
//        // 2. Collect all entities (within view range) into 1 singular list!
//        // Then check if that entity is not within the list and not dead/removed, ect...
//        // And spawn
//
//        List<Entity> allVisibleEntities = new ArrayList<>();
//        for (int chunkY = clientChunkY - GameConstants.VISIBLE_CHUNK_RADIUS; chunkY < clientChunkY + GameConstants.VISIBLE_CHUNK_RADIUS + 1; chunkY++) {
//            for (int chunkX = clientChunkX - GameConstants.VISIBLE_CHUNK_RADIUS; chunkX < clientChunkX + GameConstants.VISIBLE_CHUNK_RADIUS + 1; chunkX++) {
//                WorldChunk chunk = findChunk((short) chunkX, (short) chunkY);
//
//                if (chunk == null) continue;
//
//
//                // List<Entity> entities = chunk.getEntitiesInChunk();
//                // allVisibleEntities.addAll(entities);
//
//            }
//        }
//    }

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
        if (worldChunk == null) return true;

        int localX = entityX - worldChunk.getChunkX() * GameConstants.CHUNK_SIZE;
        int localY = entityY - worldChunk.getChunkY() * GameConstants.CHUNK_SIZE;

        return worldChunk.isTraversable(localX, localY);
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

    public boolean isSameChunk(WorldChunk chunk1, WorldChunk chunk2) {
        if (chunk1 == null) println(getClass(), "Chunk 1 null");
        if (chunk2 == null) println(getClass(), "Chunk 2 null");
        if (chunk1 == null || chunk2 == null) return false;
        return ((chunk1.getChunkX() << 16) | (chunk1.getChunkY() & 0xFFFF)) == ((chunk2.getChunkX() << 16) | (chunk2.getChunkY() & 0xFFFF));
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
