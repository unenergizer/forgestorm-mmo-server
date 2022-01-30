package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.DoorManager;
import com.forgestorm.server.game.world.maps.tile.Tile;
import com.forgestorm.server.game.world.maps.tile.TileImage;
import com.forgestorm.server.game.world.maps.tile.properties.DoorProperty;
import com.forgestorm.shared.game.world.maps.tile.properties.TilePropertyTypes;
import com.forgestorm.shared.network.game.GameOutputStream;
import com.forgestorm.shared.network.game.Opcodes;

import java.util.List;

import static com.forgestorm.server.util.Log.println;

public class TileImageStatusesPacketOut extends AbstractPacketOut {

    private static final boolean PRINT_DEBUG = true;

    private final List<Tile> tilesWithStatuses;

    public TileImageStatusesPacketOut(final Player player, List<Tile> tilesWithStatuses) {
        super(Opcodes.TILE_IMAGE_STATUSES, player.getClientHandler());
        this.tilesWithStatuses = tilesWithStatuses;
    }

    @Override
    public void createPacket(GameOutputStream write) {
        // Send number of statuses
        write.writeInt(tilesWithStatuses.size());
        println(getClass(), "Number of statuses: " + tilesWithStatuses.size(), false, PRINT_DEBUG);

        // Loop through and send
        for (Tile tile : tilesWithStatuses) {

            /* Right now only doors/gates have statuses. Currently, it is safe
            to assume every entry has a DoorProperty. */
            TileImage tileImage = tile.getTileImage();

            // Send location
            write.writeInt(tile.getWorldX());
            write.writeInt(tile.getWorldY());
            write.writeShort(tile.getWorldZ());

            println(getClass(),
                    " - Location: "
                            + tile.getWorldX() + "/"
                            + tile.getWorldY() + "/"
                            + tile.getWorldZ(), false, PRINT_DEBUG);

            // Send status
            DoorProperty doorProperty = (DoorProperty) tileImage.getProperty(TilePropertyTypes.DOOR);
            DoorManager.DoorStatus doorStatus = doorProperty.getDoorStatus();
            byte ordinal = DoorManager.DoorStatus.getByte(doorProperty.getDoorStatus());
            write.writeByte(ordinal);

            println(getClass(), " - Status: " + doorStatus + ", ByteIndex: " + ordinal, false, PRINT_DEBUG);
        }
    }
}
