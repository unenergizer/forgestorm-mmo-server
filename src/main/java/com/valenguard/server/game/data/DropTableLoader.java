package com.valenguard.server.game.data;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.valenguard.server.util.Log.println;

public class DropTableLoader {

    private static final boolean PRINT_DEBUG = true;
    private static final String FILE_PATH = "src/main/resources/data/item/DropTables.yaml";

    public List<DropTable> loadDropTables() {
        println(getClass(), "====== START LOADING DROP TABLES ======", false, PRINT_DEBUG);
        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(FILE_PATH));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<String, Map<String, Object>> root = yaml.load(inputStream);

        List<DropTable> dropTables = new ArrayList<>();

        for (Map.Entry<String, Map<String, Object>> entry : root.entrySet()) {
            int dropTableID = Integer.parseInt(entry.getKey());
            Map<String, Object> itemNode = entry.getValue();

            DropTable dropTable = new DropTable(dropTableID);

            /*
             * Get universal item information
             */
            Integer itemID = (Integer) itemNode.get("item");

            dropTable.setItemStackID(itemID);

            println(getClass(), "DropTableID: " + dropTableID, false, PRINT_DEBUG);
            println(getClass(), "ItemStackID: " + itemID, false, PRINT_DEBUG);
            println(PRINT_DEBUG);

            dropTables.add(dropTable);
        }

        println(getClass(), "====== END LOADING DROP TABLES ======", false, PRINT_DEBUG);
        return dropTables;
    }
}
