package com.valenguard.server.io;

import com.valenguard.server.Server;
import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.rpg.StationaryTypes;
import com.valenguard.server.game.world.entity.*;
import com.valenguard.server.game.world.maps.*;
import com.valenguard.server.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@SuppressWarnings("ConstantConditions")
public class TmxFileParser {

    private static final short TILE_SIZE = 16;
    private static final boolean PRINT_DEBUG = false;

    /**
     * This takes in a TMX map and gets the collision elements from it and builds a collision
     * array for checking entity collision server side.
     *
     * @param directory The directory that contains this map.
     * @param fileName  The name of the TMX map file.
     * @return A map io class with information about this map.
     */
    @SuppressWarnings("UnusedAssignment")
    public static GameMap parseGameMap(String directory, String fileName) {

        // Lets get the document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        Document document = null;
        try {
            //noinspection ConstantConditions
            document = builder.parse(directory + fileName + ".tmx");
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        // Get the first element
        //noinspection ConstantConditions
        Element tmx = document.getDocumentElement();


        /* *********************************************************************************************
         * BUILD TILED MAP TILES......
         ***********************************************************************************************/

        final short mapWidth = Short.parseShort(tmx.getAttributes().getNamedItem("width").getNodeValue());
        final short mapHeight = Short.parseShort(tmx.getAttributes().getNamedItem("height").getNodeValue());

        Log.println(TmxFileParser.class, "MapWidth: " + mapWidth, false, PRINT_DEBUG);
        Log.println(TmxFileParser.class, "MapHeight: " + mapHeight, false, PRINT_DEBUG);

        Tile map[][] = new Tile[mapWidth][mapHeight];

        // Examine XML file and find tags called "layer" then loop through them.
        NodeList layerTag = tmx.getElementsByTagName("layer");

        for (int i = 0; i < layerTag.getLength(); i++) {

            // Get collision layer
            if (((Element) layerTag.item(i)).getAttribute("name").equals("collision")) {

                // Get collision io
                NodeList collisionData = ((Element) layerTag.item(i)).getElementsByTagName("data");

                // Get Array of tiles
                String[] tiles = collisionData.item(0).getTextContent().split(",");

                // Loop through all tiles
                // Start iteration from top left
                int currentY = mapHeight - 1; // using map height instead of zero, because we are starting at top.
                int currentX = 0; // using 0 because we are starting from the left

                for (String tile : tiles) {

                    tile = tile.trim();

                    // Tile ID
                    int tileType = Integer.parseInt(tile);

                    // Basic coordinate system of our tiled map.
                    // Iteration starts at top left.
                    // So if map height is 50, we start fromY at 50
                    // and then count down (moving down the y access).
                    // Since X starts on the left, we will start it at 0.
                    //
                    // [X,Y]
                    //
                    //////////////////////////////////////
                    // [0,5] [1,5] [2,5] [3,5] [4,5] [5,5]
                    // [0,4] [1,4] [2,4] [3,4] [4,4] [5,4]
                    // [0,3] [1,3] [2,3] [3,3] [4,3] [5,3]
                    // [0,2] [1,2] [2,2] [3,2] [4,2] [5,2]
                    // [0,1] [1,1] [2,1] [3,1] [4,1] [5,1]
                    // [0,0] [1,0] [2,0] [3,0] [4,0] [5,0]

                    // Initializing the instance of the new tile
                    map[currentX][currentY] = new Tile();

                    // Check for tile ID and add it to collision map
                    if (tileType != 0) {
                        map[currentX][currentY].setTraversable(false); // Not traversable
                        //System.out.print("#");
                    } else {
                        map[currentX][currentY].setTraversable(true); // Is traversable
                        //System.out.print(" ");
                    }

                    // Increment x horizontal value
                    currentX++;

                    // Check for end of map width
                    if (currentX == mapWidth) {
                        //Log.sendMessage(TmxFileParser.class,("");
                        currentX = 0; // reset x counter
                        currentY--; // decrement y value
                    }
                }
            }
        }

        /* *********************************************************************************************
         * GET SPECIFIC TILE ATTRIBUTES - WARNING: Element names are CASE sensitive!
         ***********************************************************************************************/

        // Examine XML file and find tags called "layer" then loop through them.
        NodeList objectGroupTag = tmx.getElementsByTagName("objectgroup");

        for (int i = 0; i < objectGroupTag.getLength(); i++) {

            /*
             * Get Entities
             */
            if (((Element) objectGroupTag.item(i)).getAttribute("name").equals("entity")) {
                NodeList objectTag = ((Element) objectGroupTag.item(i)).getElementsByTagName("object");
                for (int j = 0; j < objectTag.getLength(); j++) {
                    if (objectTag.item(j).getNodeType() != Node.ELEMENT_NODE) continue;

                    Element objectTagElement = (Element) objectTag.item(j);

                    // Initialize AiEntity vars
                    short x = (short) (Short.parseShort(objectTagElement.getAttribute("x")) / TILE_SIZE);
                    short y = (short) (mapHeight - (Short.parseShort(objectTagElement.getAttribute("y")) / TILE_SIZE) - 1);
                    short bounds1x = -2;
                    short bounds1y = -2;
                    short bounds2x = -2;
                    short bounds2y = -2;
                    MoveDirection direction = MoveDirection.SOUTH;
                    int aiEntityDataID = -1;

                    // Get custom properties
                    NodeList properties = objectTagElement.getElementsByTagName("properties").item(0).getChildNodes();
                    for (int k = 0; k < properties.getLength(); k++) {
                        if (properties.item(k).getNodeType() != Node.ELEMENT_NODE) continue;
                        Element propertyElement = (Element) properties.item(k);

                        if (propertyElement.getAttribute("name").equals("direction")) {
                            direction = MoveDirection.valueOf(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("bounds1x")) {
                            bounds1x = Short.parseShort(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("bounds1y")) {
                            bounds1y = Short.parseShort(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("bounds2x")) {
                            bounds2x = Short.parseShort(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("bounds2y")) {
                            bounds2y = Short.parseShort(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("aiEntityDataID")) {
                            aiEntityDataID = Integer.parseInt(propertyElement.getAttribute("value"));
                        }
                    }

                    // Build the entity!
                    AiEntityLoader.AiEntityData aiEntityData = Server.getInstance().getAiEntityDataManager().getEntityData(aiEntityDataID);

                    AiEntity aiEntity = null;

                    if (aiEntityData.getEntityType() == EntityType.MONSTER) {
                        aiEntity = new Monster();
                        ((Monster) aiEntity).setAlignment(aiEntityData.getEntityAlignment());
                    } else if (aiEntityData.getEntityType() == EntityType.NPC) {
                        aiEntity = new NPC();
                        ((NPC) aiEntity).setFaction(Server.getInstance().getFactionManager().getFactionByName(aiEntityData.getFaction()));
                    }

                    aiEntity.setName(aiEntityData.getName());
                    aiEntity.setEntityType(aiEntityData.getEntityType());
                    aiEntity.setCurrentHealth(aiEntityData.getHealth());
                    aiEntity.setMaxHealth(aiEntityData.getHealth());
                    aiEntity.setExpDrop(aiEntityData.getExpDrop());
                    aiEntity.setDropTable(aiEntityData.getDropTable());
                    aiEntity.setMoveSpeed(aiEntityData.getWalkSpeed());
                    aiEntity.setBankKeeper(aiEntityData.isBankKeeper());

                    aiEntity.setMovementInfo(aiEntityData.getProbabilityStill(), aiEntityData.getProbabilityWalkStart(), bounds1x, mapHeight - bounds1y - 1, bounds2x, mapHeight - bounds2y - 1);
                    aiEntity.setSpawnWarp(new Warp(new Location(fileName, x, y), direction));
                    aiEntity.gameMapRegister(aiEntity.getSpawnWarp());

                    // Setup appearance
                    byte colorID = 0;
                    if (aiEntityData.getColorID() != null) {
                        colorID = (byte) (int) aiEntityData.getColorID();
                    }

                    short[] appearanceTextureIds;
                    if (aiEntityData.getAtlasHeadID() != null) {
                        appearanceTextureIds = new short[2];
                        appearanceTextureIds[Appearance.BODY] = aiEntityData.getAtlasBodyID();
                        appearanceTextureIds[Appearance.HEAD] = (short) (int) aiEntityData.getAtlasHeadID();
                        aiEntity.setAppearance(new Appearance(aiEntity, colorID, appearanceTextureIds));
                    } else {
                        appearanceTextureIds = new short[1];
                        appearanceTextureIds[Appearance.BODY] = aiEntityData.getAtlasBodyID();
                        aiEntity.setAppearance(new Appearance(aiEntity, colorID, appearanceTextureIds));
                    }

                    // Setup basic attributes.
                    Attributes attributes = new Attributes();
                    attributes.setDamage(aiEntityData.getDamage());
                    aiEntity.setAttributes(attributes);

                    if (aiEntityData.getShopID() != null) aiEntity.setShopId((short) (int) aiEntityData.getShopID());

                    // Queue Mob Spawn
                    Server.getInstance().getGameManager().getGameMapProcessor().queueAiEntitySpawn(aiEntity);

                    Log.println(TmxFileParser.class, "[Entity] AiEntityData: " + aiEntityDataID + ", X: " + x + ", Y: " + y + ", b1X: " + bounds1x + ", b1Y: " + bounds1y + ", b2X: " + bounds2x + ", b2Y: " + bounds2y, false, PRINT_DEBUG);
                }
            }

            /*
             *  Get BANK ACCESS
             */
            if (((Element) objectGroupTag.item(i)).getAttribute("name").equals("bank_access")) {

                NodeList objectTag = ((Element) objectGroupTag.item(i)).getElementsByTagName("object");

                for (int j = 0; j < objectTag.getLength(); j++) {
                    if (objectTag.item(j).getNodeType() != Node.ELEMENT_NODE) continue;

                    Element objectTagElement = (Element) objectTag.item(j);

                    short x = (short) (Short.parseShort(objectTagElement.getAttribute("x")) / GameConstants.TILE_SIZE);
                    short y = (short) (mapHeight - (Short.parseShort(objectTagElement.getAttribute("y")) / GameConstants.TILE_SIZE) - 1);

                    // Making it's associated tile non-traversable
                    map[x][y].setBankAccess(true);

                }
            }

            /*
             * Get SKILL NODES
             */
            if (((Element) objectGroupTag.item(i)).getAttribute("name").equals("skill")) {
                NodeList objectTag = ((Element) objectGroupTag.item(i)).getElementsByTagName("object");
                for (int j = 0; j < objectTag.getLength(); j++) {

                    //System.out.sendMessage("NodeType: " + objectTag.item(j).getNodeType());
                    if (objectTag.item(j).getNodeType() != Node.ELEMENT_NODE) continue;

                    Element objectTagElement = (Element) objectTag.item(j);

                    short x = (short) (Short.parseShort(objectTagElement.getAttribute("x")) / TILE_SIZE);
                    short y = (short) (mapHeight - (Short.parseShort(objectTagElement.getAttribute("y")) / TILE_SIZE) - 1);

                    String typeID = null;

                    NodeList properties = objectTagElement.getElementsByTagName("properties").item(0).getChildNodes();

                    // Get custom properties
                    for (int k = 0; k < properties.getLength(); k++) {
                        if (properties.item(k).getNodeType() != Node.ELEMENT_NODE) continue;
                        Element propertyElement = (Element) properties.item(k);

                        if (propertyElement.getAttribute("name").equals("TYPE")) {
                            typeID = propertyElement.getAttribute("value");
                        }
                    }

                    // TODO: SEND SKILL NODE OFF TO BE MANAGED. DECIPHER TYPE IN SPECIFIC MANAGER (reduce code here)
                    StationaryEntity stationaryEntity = new StationaryEntity();
                    stationaryEntity.setCurrentMapLocation(new Location(fileName, x, y));
                    stationaryEntity.setEntityType(EntityType.SKILL_NODE);
                    stationaryEntity.setStationaryType(StationaryTypes.valueOf(typeID));
                    stationaryEntity.setAppearance(new Appearance(stationaryEntity, (byte) 0, new short[]{0})); // todo determine texture id
                    stationaryEntity.setName(""); // todo will these even have names?
                    Server.getInstance().getGameManager().getGameMapProcessor().queueStationaryEntitySpawn(stationaryEntity);

                    // Making it's associated tile non-traversable
                    map[x][y].setTraversable(false);

                    Log.println(TmxFileParser.class, "[SILL] TYPE: " + typeID, false, PRINT_DEBUG);
                }
            }

            /*
             * Get Warps
             */
            if (((Element) objectGroupTag.item(i)).getAttribute("name").equals("warp")) {

                NodeList objectTag = ((Element) objectGroupTag.item(i)).getElementsByTagName("object");

                for (int j = 0; j < objectTag.getLength(); j++) {
                    if (objectTag.item(j).getNodeType() != Node.ELEMENT_NODE) continue;

                    Element objectTagElement = (Element) objectTag.item(j);
                    String targetMap = objectTagElement.getAttribute("name");
                    short tmxFileX = (short) (Short.parseShort(objectTagElement.getAttribute("x")) / TILE_SIZE);
                    short tmxFileY = (short) (Short.parseShort(objectTagElement.getAttribute("y")) / TILE_SIZE);
                    short tmxFileWidth = (short) (Short.parseShort(objectTagElement.getAttribute("width")) / TILE_SIZE);
                    short tmxFileHeight = (short) (Short.parseShort(objectTagElement.getAttribute("height")) / TILE_SIZE);

//                    String warpMapName = null;
                    // Set to negative one to let the server know that the warp is an outbound
                    // warp to another map
                    short warpX = -1;
                    short warpY = -1;
                    MoveDirection moveDirection = null;
                    NodeList properties = objectTagElement.getElementsByTagName("properties").item(0).getChildNodes();

                    Log.println(TmxFileParser.class, "", false, PRINT_DEBUG);
                    Log.println(TmxFileParser.class, "===[ WARP ]==================================", true, PRINT_DEBUG);

                    for (int k = 0; k < properties.getLength(); k++) {

                        if (properties.item(k).getNodeType() != Node.ELEMENT_NODE) continue;
                        Element propertyElement = (Element) properties.item(k);

//                        // Get map name:
//                        if (propertyElement.getAttribute("name").equals("mapname")) {
//                            warpMapName = propertyElement.getAttribute("value");
//                            Log.sendMessage(TmxFileParser.class, "WarpMap: " + warpMapName, false, PRINT_DEBUG);
//                        }

                        // Get map X:
                        if (propertyElement.getAttribute("name").equals("x")) {
                            warpX = Short.parseShort(propertyElement.getAttribute("value"));
                            Log.println(TmxFileParser.class, "WarpX: " + warpX, false, PRINT_DEBUG);
                        }

                        // Get map Y:
                        if (propertyElement.getAttribute("name").equals("y")) {
                            warpY = Short.parseShort(propertyElement.getAttribute("value"));
                            Log.println(TmxFileParser.class, "WarpY: " + warpY, false, PRINT_DEBUG);
                        }

                        // Get map facing moveDirection:
                        if (propertyElement.getAttribute("name").equals("direction")) {
                            moveDirection = MoveDirection.valueOf(propertyElement.getAttribute("value").toUpperCase());
                            Log.println(TmxFileParser.class, "WarpDirection: " + moveDirection, false, PRINT_DEBUG);
                        }
                    }

                    // Print the map to console.
                    for (short ii = tmxFileY; ii < tmxFileY + tmxFileHeight; ii++) {
                        for (short jj = tmxFileX; jj < tmxFileX + tmxFileWidth; jj++) {
                            short tileY = (short) (mapHeight - ii - 1);
                            Tile tile = map[jj][mapHeight - ii - 1];
                            tile.setWarp(new Warp(new Location(targetMap, warpX, warpY), moveDirection));
                            Log.println(TmxFileParser.class, tile.getWarp().getLocation().getMapName(), false, PRINT_DEBUG);
                            Log.println(TmxFileParser.class, "TileX: " + jj, false, PRINT_DEBUG);
                            Log.println(TmxFileParser.class, "TileY: " + tileY, false, PRINT_DEBUG);
                        }
                    }
                }
            }
        }

        /*
         * Print the map to console.
         */
        if (PRINT_DEBUG) {
            short yOffset = (short) (mapHeight - 1);
            for (short height = yOffset; height >= 0; height--) {
                for (short width = 0; width < mapWidth; width++) {
                    Tile tile = map[width][height];
                    if (!tile.isTraversable()) System.out.print("X");
                    else if (tile.isTraversable() && tile.getWarp() != null) System.out.print("@");
                    else if (tile.isTraversable() && tile.getWarp() == null) System.out.print(" ");
                }
                System.out.println();
            }
            System.out.println(); // Clear a line for next map
        }
        return new GameMap(fileName.replace(".tmx", ""), mapWidth, mapHeight, map);
    }
}
