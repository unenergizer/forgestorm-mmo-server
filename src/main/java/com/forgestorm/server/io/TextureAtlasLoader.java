package com.forgestorm.server.io;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.forgestorm.server.io.atlas.TextureAtlas;

/**
 * HEAVILY MODIFIED!!! ONLY NEED REGION INFO, NOT A TEXTURE FOR THE SERVER...
 *
 * @author mzechner
 */
public class TextureAtlasLoader extends SynchronousAssetLoader<TextureAtlas, TextureAtlasLoader.TextureAtlasParameter> {

    static public class TextureAtlasParameter extends AssetLoaderParameters<TextureAtlas> {
    }

    TextureAtlas.TextureAtlasData data;

    public TextureAtlasLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public TextureAtlas load(AssetManager assetManager, String fileName, FileHandle file, TextureAtlasParameter parameter) {
        TextureAtlas atlas = new TextureAtlas(data);
        data = null;
        return atlas;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle atlasFile, TextureAtlasLoader.TextureAtlasParameter parameter) {
        FileHandle imgDir = atlasFile.parent();

        data = new TextureAtlas.TextureAtlasData(atlasFile, imgDir, false);

        return new Array();
    }
}
