package com.forgestorm.server.io.todo;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.forgestorm.server.game.world.maps.GameWorld;
import com.forgestorm.server.util.libgdx.Color;
import lombok.Getter;
import lombok.Setter;

public class GameWorldLoader extends AsynchronousAssetLoader<GameWorldLoader.GameWorldDataWrapper, GameWorldLoader.GameWorldParameter> {

    static class GameWorldParameter extends AssetLoaderParameters<GameWorldDataWrapper> {
    }

    private GameWorldDataWrapper gameWorldDataWrapper = null;

    GameWorldLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, GameWorldParameter parameter) {
        gameWorldDataWrapper = null;
        gameWorldDataWrapper = new GameWorldDataWrapper();
        GameWorld gameWorld = parseGameWorld(file);
        gameWorldDataWrapper.setGameWorld(gameWorld);
    }

    @Override
    public GameWorldDataWrapper loadSync(AssetManager manager, String fileName, FileHandle file, GameWorldParameter parameter) {
        return gameWorldDataWrapper;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, GameWorldParameter parameter) {
        return null;
    }

    private GameWorld parseGameWorld(FileHandle fileHandle) {

        JsonValue root = new JsonReader().parse(fileHandle.reader());

        String worldName = fileHandle.name().replace(".json", "");
        float backgroundRed = root.get("backgroundRed").asFloat();
        float backgroundGreen = root.get("backgroundGreen").asFloat();
        float backgroundBlue = root.get("backgroundBlue").asFloat();
        float backgroundAlpha = root.get("backgroundAlpha").asFloat();
        Color backgroundColor = new Color(backgroundRed, backgroundGreen, backgroundBlue, backgroundAlpha);

        return new GameWorld(fileHandle.path(), worldName, backgroundColor);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    @Setter
    @Getter
    public class GameWorldDataWrapper {
        private GameWorld gameWorld = null;
    }
}
