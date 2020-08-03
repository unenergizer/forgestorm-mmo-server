package com.forgestorm.server.scripting;


import com.forgestorm.server.io.FilePaths;
import com.forgestorm.server.io.TmxFileParser;

import java.io.*;

import static com.forgestorm.server.util.Log.println;

public class ScriptLoader {

    private static final boolean PRINT_DEBUG = false;

    public String loadScript(String fileName) {

        println(getClass(), "====== START LOADING NPC SCRIPTS ======", false, PRINT_DEBUG);

        InputStream inputStream = TmxFileParser.class.getResourceAsStream(FilePaths.SCRIPTS.getFilePath() + fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        try {

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        println(getClass(), "====== END LOADING NPC SCRIPTS ======", false, PRINT_DEBUG);
        return stringBuilder.toString();

    }
}
