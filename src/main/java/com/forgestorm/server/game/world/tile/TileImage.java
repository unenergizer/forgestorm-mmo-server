package com.forgestorm.server.game.world.tile;


import com.forgestorm.server.ServerMain;
import com.forgestorm.shared.game.world.maps.Tags;
import com.forgestorm.shared.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.tile.properties.AbstractTileProperty;
import com.forgestorm.shared.game.world.tile.properties.TilePropertyTypes;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.forgestorm.server.util.Log.println;


public class TileImage {

    private static final transient boolean PRINT_DEBUG = false;

    @Getter
    private final transient int imageId;

    @Getter
    private final String fileName;

    @Getter
    @Setter
    private Map<TilePropertyTypes, AbstractTileProperty> tileProperties;

    private List<String> tagsList;

    @Getter
    @Setter
    private LayerDefinition layerDefinition;

    public TileImage(int imageId, String fileName, LayerDefinition layerDefinition) {
        this.imageId = imageId;
        this.fileName = fileName;
        this.layerDefinition = layerDefinition;

        println(getClass(), "---- NEW TILE IMAGE CREATED ----", false, PRINT_DEBUG);
        println(getClass(), "ImageID: " + imageId, false, PRINT_DEBUG);
        println(getClass(), "FileName: " + fileName, false, PRINT_DEBUG);
        println(getClass(), "LayerDefinition: " + layerDefinition, false, PRINT_DEBUG);
    }

    public TileImage(TileImage tileImage) {
        this.imageId = tileImage.getImageId();
        this.fileName = tileImage.getFileName();
        this.layerDefinition = tileImage.getLayerDefinition();

        // Copy tile properties
        if (tileImage.getTileProperties() != null) {
            for (AbstractTileProperty entry : tileImage.getTileProperties().values()) {
                setCustomTileProperty(entry);
            }
        }

        // Copy Tags List
        if (tileImage.tagsList != null) {
            for (String tag : tileImage.tagsList) {
                addTag(com.forgestorm.shared.game.world.maps.Tags.valueOf(tag));
            }
        }
    }

    public void addTag(com.forgestorm.shared.game.world.maps.Tags tag) {
        if (tag == com.forgestorm.shared.game.world.maps.Tags.AN_UNUSED_TAG) return;
        if (tagsList == null) tagsList = new ArrayList<String>();
        if (containsTag(tag)) return;
        tagsList.add(tag.name());
    }

    public boolean containsTag(com.forgestorm.shared.game.world.maps.Tags tag) {
        if (tagsList == null) return false;
        for (String s : tagsList) if (s.equals(tag.name())) return true;
        return false;
    }

    public void removeTag(Tags tag) {
        if (tagsList == null) return;
        tagsList.remove(tag.name());
    }

    public boolean containsProperty(TilePropertyTypes tilePropertyType) {
        if (tileProperties == null || tileProperties.isEmpty()) return false;
        return tileProperties.containsKey(tilePropertyType);
    }

    public AbstractTileProperty getProperty(TilePropertyTypes tilePropertyTypes) {
        return this.tileProperties.get(tilePropertyTypes);
    }

    public void setCustomTileProperty(AbstractTileProperty customTileProperty) {
        // Only create an instance of the HashMap here!
        // Doing so will keep the YAML saving code from producing empty brackets
        // in the TileProperties.yaml document.
        if (tileProperties == null) {
            tileProperties = new HashMap<>();
        }

        if (tileProperties.containsKey(customTileProperty.getTilePropertyType())) {
            println(getClass(), "TilePropertiesMap already contains this property: " + customTileProperty.getTilePropertyType(), true);
        } else {
            tileProperties.put(customTileProperty.getTilePropertyType(), customTileProperty);
        }

        if (PRINT_DEBUG) {
            println(getClass(), "---- TILE IMAGE SET PROPERTY ----", false);
            println(getClass(), "ImageID: " + imageId, false);
            println(getClass(), "FileName: " + fileName, false);
            println(getClass(), "LayerDefinition: " + layerDefinition, false);

            for (AbstractTileProperty abstractTileProperty : tileProperties.values()) {
                println(getClass(), "Property: " + abstractTileProperty.getTilePropertyType().toString());
            }
        }
    }

    public int getWidth() {
        int width = ServerMain.getInstance().getWorldBuilder().getWorldTileImages().findRegion(fileName).originalWidth;
        println(getClass(), "TileImage:" + fileName + ", Width: " + width, false, PRINT_DEBUG);
        return width;
    }

    public int getHeight() {
        int height = ServerMain.getInstance().getWorldBuilder().getWorldTileImages().findRegion(fileName).originalHeight;
        println(getClass(), "TileImage:" + fileName + ", Height: " + height, false, PRINT_DEBUG);
        return height;
    }

    @Override
    public String toString() {
        return "FileName: " + fileName + ", ImageID: " + imageId + ", Layer: " + layerDefinition + ", Properties: " + tileProperties.size();
    }
}
