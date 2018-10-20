package com.valenguard.server.maps.file;

import com.valenguard.server.entity.Entity;
import com.valenguard.server.maps.data.Location;
import com.valenguard.server.maps.data.TmxMap;
import com.valenguard.server.maps.data.Tile;
import com.valenguard.server.maps.data.Warp;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TmxFileParser {

    private static final int TILE_SIZE = 16;
    private static final boolean PRINT_MAP = false;

    private static short entityCount = 1000;

    /**
     * This takes in a TMX map and gets the collision elements from it and builds a collision
     * array for checking entity collision server side.
     *
     * @param directory The directory that contains this map.
     * @param fileName  The name of the TMX map file.
     * @return A map data class with information about this map.
     */
    public static TmxMap loadXMLFile(String directory, String fileName) {

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
            document = builder.parse(directory + fileName);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        // Get the first element
        Element tmx = document.getDocumentElement();


        /************************************************************************************************
         * BUILD TILED MAP TILES......
         ***********************************************************************************************/

        int mapWidth = Integer.parseInt(tmx.getAttributes().getNamedItem("width").getNodeValue());
        int mapHeight = Integer.parseInt(tmx.getAttributes().getNamedItem("height").getNodeValue());

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
                        //System.out.println("");
                        currentX = 0; // reset x counter
                        currentY--; // decrement y value
                    }
                }
            }
        }

        /************************************************************************************************
         * GET SPECIFIC TILE ATTRIBUTES
         ***********************************************************************************************/

        // Examine XML file and find tags called "layer" then loop through them.
        NodeList objectgroupTag = tmx.getElementsByTagName("objectgroup");
        List<Entity> entityList = new ArrayList<>();

        for (int i = 0; i < objectgroupTag.getLength(); i++) {


            // Get entities
            if (((Element) objectgroupTag.item(i)).getAttribute("name").equals("entities")) {
                NodeList objectTag = ((Element) objectgroupTag.item(i)).getElementsByTagName("object");

                System.out.println("Found " + objectgroupTag.getLength() + " entity spawns.");

                for (int j = 0; j < objectTag.getLength(); j++) {

                    //System.out.println("NodeType: " + objectTag.item(j).getNodeType());

                    if (objectTag.item(j).getNodeType() != Node.ELEMENT_NODE) continue;

                    Element objectTagElement = (Element) objectTag.item(j);
                    String name = objectTagElement.getAttribute("name");
                    int x = Integer.parseInt(objectTagElement.getAttribute("x")) / TILE_SIZE;
                    int y = Integer.parseInt(objectTagElement.getAttribute("y")) / TILE_SIZE;

                    System.out.println("[Entity] name: " + name + ", X: " + x + ", Y: " + y);

                    // todo abstract this out so that we only call new Entity in a single place
                    //entityList.add(new Entity(entityCount, new Location(fileName, x, y), 1));

                    entityCount++;
                }
            }

            // Get warps
            if (((Element) objectgroupTag.item(i)).getAttribute("name").equals("warp")) {


                NodeList objectTag = ((Element) objectgroupTag.item(i)).getElementsByTagName("object");

//                System.out.println("WARPS: " + objectTag.getLength());

                for (int j = 0; j < objectTag.getLength(); j++) {

                    //System.out.println("NodeType: " + objectTag.item(j).getNodeType());

                    if (objectTag.item(j).getNodeType() != Node.ELEMENT_NODE) continue;

                    Element objectTagElement = (Element) objectTag.item(j);
                    int x = Integer.parseInt(objectTagElement.getAttribute("x")) / TILE_SIZE;
                    int y = Integer.parseInt(objectTagElement.getAttribute("y")) / TILE_SIZE;
                    int width = Integer.parseInt(objectTagElement.getAttribute("width")) / TILE_SIZE;
                    int height = Integer.parseInt(objectTagElement.getAttribute("height")) / TILE_SIZE;

                    String warpMapName = null;
                    // Set to negative one to let the server know that the warp is an outbound
                    // warp to another map
                    int warpX = -1;
                    int warpY = -1;

//                    System.out.println("[WARP #" + j + "] X: " + x + ", Y: " + y + ", Width: " + width + ", Height: " + height);

                    NodeList properties = objectTagElement.getElementsByTagName("properties").item(0).getChildNodes();

                    for (int k = 0; k < properties.getLength(); k++) {

                        if (properties.item(k).getNodeType() != Node.ELEMENT_NODE) continue;
                        Element propertyElement = (Element) properties.item(k);

                        // Get map name:
                        if (propertyElement.getAttribute("name").equals("map")) {
                            warpMapName = propertyElement.getAttribute("value");
//                            System.out.println("Map: " + warpMapName);
                        }

                        // Get map X:
                        if (propertyElement.getAttribute("name").equals("X")) {
                            warpX = Integer.parseInt(propertyElement.getAttribute("value"));
//                            System.out.println("X: " + warpX);
                        }

                        // Get map Y:
                        if (propertyElement.getAttribute("name").equals("Y")) {
                            warpY = Integer.parseInt(propertyElement.getAttribute("value"));
//                            System.out.println("Y: " + warpY);
                        }

                        // Print the map to console.
                        for (int ii = y; ii < y + height; ii++) {
                            for (int jj = x; jj < x + width; jj++) {
                                Tile tile = map[jj][mapHeight - ii - 1];
                                tile.setWarp(new Warp(warpMapName + ".tmx", warpX, warpY));
//                                System.out.println(tile.getWarp().getMapName());
//                                System.out.println(tile.getWarp().getX());
//                                System.out.println(tile.getWarp().getY());
                            }
                        }
                    }
                }
            }
        }

        /**
         * Print the map to console.
         */
        if (PRINT_MAP) {
            int yOffset = mapHeight - 1;
            for (int height = yOffset; height >= 0; height--) {
                for (int width = 0; width < mapWidth; width++) {

                    Tile tile = map[width][height];

                    boolean entityFound = false;

                    for (Entity entity : entityList) {
                        Location location = entity.getLocation();
                        if (location.getX() == width && location.getY() == yOffset - height) {
                            System.out.print("e");
                            entityFound = true;
                        }
                    }

                    if (!tile.isTraversable()) {
                        System.out.print("X");

                    } else if (tile.isTraversable() && tile.getWarp() != null) {
                        System.out.print("@");

                    } else if (tile.isTraversable() && tile.getWarp() == null && !entityFound) {
                        System.out.print(" ");
                    }

                }

                System.out.println();
            }

            System.out.println(); // Clear a line for next map
        }
        return new TmxMap(fileName, mapWidth, mapHeight, map, entityList);
    }
}
