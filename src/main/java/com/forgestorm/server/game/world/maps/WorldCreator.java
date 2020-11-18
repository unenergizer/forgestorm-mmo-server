package com.forgestorm.server.game.world.maps;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.forgestorm.server.ServerMain;
import com.forgestorm.server.util.RandomUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import static com.forgestorm.server.util.Log.println;

public class WorldCreator {


    public void createWorld(String worldName, int chunkWidth, int chunkHeight) {
        String worldDirectoryPath = ServerMain.getInstance().getFileManager().getWorldDirectory();

        File worldDirectory = new File(worldDirectoryPath);

        createMainWorldFile(worldDirectory, worldName, chunkWidth, chunkHeight);
        createWorldChunks(worldDirectory, worldName, chunkWidth, chunkHeight);
    }

    private void createMainWorldFile(File worldDirectory, String worldName, int chunkWidth, int chunkHeight) {
        File worldFile = new File(worldDirectory + File.separator + worldName + ".json");
        try {
            if (!worldFile.createNewFile()) {
                throw new RuntimeException("World file \"" + worldName + ".json\" could not be made.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Json json = new Json();
        StringWriter jsonText = new StringWriter();
        JsonWriter writer = new JsonWriter(jsonText);
        json.setOutputType(JsonWriter.OutputType.json);
        json.setWriter(writer);
        json.writeObjectStart();
        json.writeValue("backgroundRed", RandomUtil.getNewRandom());
        json.writeValue("backgroundGreen", RandomUtil.getNewRandom());
        json.writeValue("backgroundBlue", RandomUtil.getNewRandom());
        json.writeValue("backgroundAlpha", RandomUtil.getNewRandom());
        json.writeValue("widthInChunks", chunkWidth);
        json.writeValue("heightInChunks", chunkHeight);
        json.writeObjectEnd();

        FileHandle fileHandle = new FileHandle(worldFile);
        fileHandle.writeString(json.prettyPrint(json.getWriter().getWriter().toString()), false);
    }

    private void createWorldChunks(File worldDirectory, String worldName, int chunkWidth, int chunkHeight) {
        File chunkDirectory = new File(worldDirectory + File.separator + worldName);
        if (chunkDirectory.exists()) {
            println(getClass(), "Chunk directory \"" + worldName + "\" already exists.");
        } else {
            if (!chunkDirectory.mkdir()) {
                throw new RuntimeException("Chunk directory \"" + worldName + "\" could not be made.");
            }
        }

        for (int i = 0; i < chunkWidth; i++) {
            for (int j = 0; j < chunkHeight; j++) {

                File chunkFile = new File(chunkDirectory + File.separator + i + "." + j + ".json");
                try {
                    if (!chunkFile.createNewFile()) {
                        throw new RuntimeException("World file \"" + worldName + ".json\" could not be made.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Json json = new Json();
                StringWriter jsonText = new StringWriter();
                JsonWriter writer = new JsonWriter(jsonText);
                json.setOutputType(JsonWriter.OutputType.json);
                json.setWriter(writer);
                json.writeObjectStart();
                json.writeValue("ground", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
                json.writeObjectEnd();

                FileHandle fileHandle = new FileHandle(chunkFile);
                fileHandle.writeString(json.prettyPrint(json.getWriter().getWriter().toString()), false);
            }
        }
    }

}
