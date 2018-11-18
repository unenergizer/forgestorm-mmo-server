package com.valenguard.server.game.maps;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.entity.*;
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

public class TmxFileParser {

    private static final int TILE_SIZE = 16;
    private static final boolean PRINT_DEBUG = false;

    /**
     * This takes in a TMX map and gets the collision elements from it and builds a collision
     * array for checking entity collision server side.
     *
     * @param directory The directory that contains this map.
     * @param fileName  The name of the TMX map file.
     * @return A map data class with information about this map.
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

        final int mapWidth = Integer.parseInt(tmx.getAttributes().getNamedItem("width").getNodeValue());
        final int mapHeight = Integer.parseInt(tmx.getAttributes().getNamedItem("height").getNodeValue());

        Log.println(TmxFileParser.class, "MapWidth: " + mapWidth, false, PRINT_DEBUG);
        Log.println(TmxFileParser.class, "MapHeight: " + mapHeight, false, PRINT_DEBUG);

        Tile map[][] = new Tile[mapWidth][mapHeight];

        // Examine XML file and find tags called "layer" then loop through them.
        NodeList layerTag = tmx.getElementsByTagName("layer");

        for (int i = 0; i < layerTag.getLength(); i++) {

            // Get collision layer
            if (((Element) layerTag.item(i)).getAttribute("name").equals("collision")) {

                // Get collision data
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
                        //Log.println(TmxFileParser.class,("");
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

                    //System.out.println("NodeType: " + objectTag.item(j).getNodeType());
                    if (objectTag.item(j).getNodeType() != Node.ELEMENT_NODE) continue;

                    Element objectTagElement = (Element) objectTag.item(j);
                    String name = objectTagElement.getAttribute("name");

                    int x = Integer.parseInt(objectTagElement.getAttribute("x")) / TILE_SIZE;
                    int y = mapHeight - (Integer.parseInt(objectTagElement.getAttribute("y")) / TILE_SIZE) - 1;
                    EntityType entityType = EntityType.valueOf(objectTagElement.getAttribute("type"));

                    float speed = 1f;
                    int bounds1x = -2;
                    int bounds1y = -2;
                    int bounds2x = -2;
                    int bounds2y = -2;
                    MoveDirection direction = MoveDirection.SOUTH;
                    short atlasHeadId = 0;
                    short atlasBodyId = 0;
                    float probabilityStill = -2f;
                    float probabilityWalkStart = -2f;

                    NodeList properties = objectTagElement.getElementsByTagName("properties").item(0).getChildNodes();


                    // Get custom properties
                    for (int k = 0; k < properties.getLength(); k++) {
                        if (properties.item(k).getNodeType() != Node.ELEMENT_NODE) continue;
                        Element propertyElement = (Element) properties.item(k);

                        if (propertyElement.getAttribute("name").equals("atlasHeadId")) {
                            atlasHeadId = Short.parseShort(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("atlasBodyId")) {
                            atlasBodyId = Short.parseShort(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("probabilityStill")) {
                            probabilityStill = Float.parseFloat(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("probabilityWalkStart")) {
                            probabilityWalkStart = Float.parseFloat(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("direction")) {
                            direction = MoveDirection.valueOf(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("speed")) {
                            speed = Float.parseFloat(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("bounds1x")) {
                            bounds1x = Integer.parseInt(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("bounds1y")) {
                            bounds1y = Integer.parseInt(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("bounds2x")) {
                            bounds2x = Integer.parseInt(propertyElement.getAttribute("value"));
                        }
                        if (propertyElement.getAttribute("name").equals("bounds2y")) {
                            bounds2y = Integer.parseInt(propertyElement.getAttribute("value"));
                        }
                    }

                    AIEntity aiEntity = null;
                    if (entityType == EntityType.NPC) {
                        aiEntity = new Npc();
                        aiEntity.setAppearance(new Appearance(new short[]{atlasHeadId, atlasBodyId}));
                    } else if (entityType == EntityType.MONSTER) {
                        aiEntity = new Monster();
                        aiEntity.setAppearance(new Appearance(new short[]{atlasBodyId}));
                    }

                    if (bounds1x == -2 || bounds1x == -1) {
                        aiEntity.setDefaultMovement();
                    } else if (probabilityStill != -2f && probabilityStill != -1f) {
                        aiEntity.setMovementInfo(probabilityStill, probabilityWalkStart, bounds1x, mapHeight - bounds1y - 1, bounds2x, mapHeight - bounds2y - 1);
                    } else {
                        aiEntity.setMovementBounds(bounds1x, mapHeight - bounds1y - 1, bounds2x, mapHeight - bounds2y - 1);
                    }

                    aiEntity.setServerEntityId((short) j);
                    aiEntity.setEntityType(entityType);
                    aiEntity.setName(name);
                    aiEntity.setMoveSpeed(speed);
                    aiEntity.gameMapRegister(new Warp(new Location(fileName, x, y), direction));
                    ValenguardMain.getInstance().getGameManager().queueNpcAdd(aiEntity);

                    Log.println(TmxFileParser.class, "[Entity] ID: " + j + ", name: " + name + ", probabilityStill: " + probabilityStill + ", probabilityWalkStart: " + probabilityWalkStart + ", speed: " + speed + ", X: " + x + ", Y: " + y + ", b1X: " + bounds1x + ", b1Y: " + bounds1y + ", b2X: " + bounds2x + ", b2Y: " + bounds2y, false, PRINT_DEBUG);

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
                    int tmxFileX = Integer.parseInt(objectTagElement.getAttribute("x")) / TILE_SIZE;
                    int tmxFileY = Integer.parseInt(objectTagElement.getAttribute("y")) / TILE_SIZE;
                    int tmxFileWidth = Integer.parseInt(objectTagElement.getAttribute("width")) / TILE_SIZE;
                    int tmxFileHeight = Integer.parseInt(objectTagElement.getAttribute("height")) / TILE_SIZE;

//                    String warpMapName = null;
                    // Set to negative one to let the server know that the warp is an outbound
                    // warp to another map
                    int warpX = -1;
                    int warpY = -1;
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
//                            Log.println(TmxFileParser.class, "WarpMap: " + warpMapName, false, PRINT_DEBUG);
//                        }

                        // Get map X:
                        if (propertyElement.getAttribute("name").equals("x")) {
                            warpX = Integer.parseInt(propertyElement.getAttribute("value"));
                            Log.println(TmxFileParser.class, "WarpX: " + warpX, false, PRINT_DEBUG);
                        }

                        // Get map Y:
                        if (propertyElement.getAttribute("name").equals("y")) {
                            warpY = Integer.parseInt(propertyElement.getAttribute("value"));
                            Log.println(TmxFileParser.class, "WarpY: " + warpY, false, PRINT_DEBUG);
                        }

                        // Get map facing moveDirection:
                        if (propertyElement.getAttribute("name").equals("direction")) {
                            moveDirection = MoveDirection.valueOf(propertyElement.getAttribute("value").toUpperCase());
                            Log.println(TmxFileParser.class, "WarpDirection: " + moveDirection, false, PRINT_DEBUG);
                        }
                    }

                    // Print the map to console.
                    for (int ii = tmxFileY; ii < tmxFileY + tmxFileHeight; ii++) {
                        for (int jj = tmxFileX; jj < tmxFileX + tmxFileWidth; jj++) {
                            int tileY = mapHeight - ii - 1;
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
            int yOffset = mapHeight - 1;
            for (int height = yOffset; height >= 0; height--) {
                for (int width = 0; width < mapWidth; width++) {
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
