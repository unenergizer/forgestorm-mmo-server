package com.forgestorm.server.scripting;

import com.forgestorm.server.io.FilePaths;
import com.forgestorm.server.io.ResourceList;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public class ScriptManager {

    private ScriptLoader scriptLoader;

    private static final Map<Integer, String> LOADED_SCRIPTS = new HashMap<>();

    public ScriptManager() {
        scriptLoader = new ScriptLoader();
        Map<String, Integer> scriptNameMapping = loadScriptNameMapping();

        loadDesktop(scriptNameMapping);

    }

    private int loadIdeVersion() {
        File[] files = new File("src/main/resources/" + FilePaths.SCRIPTS.getFilePath()).listFiles((d, name) -> name.endsWith(".js"));
        checkNotNull(files, "No game scripts were loaded.");

        for (File file : files) {
            String script = scriptLoader.loadScript(file.getName());

        }

        return files.length;
    }

    private int loadJarVersion() {
        Collection<String> files = ResourceList.getDirectoryResources(FilePaths.SCRIPTS.getFilePath(), ".js");
        checkNotNull(files, "No game scripts were loaded.");
        Map<String, Integer> mapping = loadScriptNameMapping();

        for (String fileName : files) {
            String[] temp = fileName.split("/"); // Removes the path
            String script = scriptLoader.loadScript(temp[temp.length - 1]);
            int scriptId = mapping.get(fileName);
            LOADED_SCRIPTS.put(scriptId, script);
        }

        return files.size();
    }

    private Map<String, Integer> loadScriptNameMapping() {

//        FileHandle fileHandle = Gdx.files.internal(FilePaths.SCRIPT_MAPPING.getFilePath());
//        Yaml yaml = new Yaml();
//        Map<Integer, String> root = yaml.load(fileHandle.read());

        Map<String, Integer> reverse = new HashMap<String, Integer>();
//        for (Map.Entry<Integer, String> entry : root.entrySet()) {
//            reverse.put(entry.getValue(), entry.getKey());
//        }

        return reverse;
    }

    private void loadDesktop(Map<String, Integer> scriptNameMapping) {
//        Collection<String> files = ResourceList.getDirectoryResources(FilePaths.SCRIPTS.getFilePath(), ".js");

//        for (String fileName : files) {
            //println(getClass(),  fileName);
            //String mapName = fileName.substring(FilePaths.MAPS.getFilePath().length() + 1);
            //FileHandle fileHandle = Gdx.files.internal(FilePaths.MAPS.getFilePath() + "/" + mapName);
            // gameMaps.put(mapName.replace(".tmx", ""), TmxFileParser.loadXMLFile(fileHandle));
//        }
    }

    public String getScript(int scriptId) {
        return LOADED_SCRIPTS.get(scriptId);
    }
}
