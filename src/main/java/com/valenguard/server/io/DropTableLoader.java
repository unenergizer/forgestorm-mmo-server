package com.valenguard.server.io;

import lombok.Getter;
import lombok.Setter;
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

    private static final boolean PRINT_DEBUG = false;

    public List<DropTable> loadDropTables() {
        println(getClass(), "====== START LOADING DROP TABLES ======", false, PRINT_DEBUG);
        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(FilePaths.DROP_TABLE.getFilePath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<Integer, Map<Integer, Map<String, Object>>> root = yaml.load(inputStream);

        List<DropTable> dropTables = new ArrayList<>();

        for (Map.Entry<Integer, Map<Integer, Map<String, Object>>> entry : root.entrySet()) {
            int dropTableID = entry.getKey();
            Map<Integer, Map<String, Object>> dropNode = entry.getValue();

            DropTable dropTable = new DropTable(dropTableID);

            int[] itemStackIDs = new int[dropNode.size()];
            float[] probabilities = new float[dropNode.size()];
            for (Map.Entry<Integer, Map<String, Object>> itemDrop : dropNode.entrySet()) {
                Map<String, Object> itemInfo = itemDrop.getValue();
                probabilities[itemDrop.getKey()] = (float) (double) (Double) itemInfo.get("prob");
                itemStackIDs[itemDrop.getKey()] = (int) itemInfo.get("item");
            }

            dropTable.setItemStackIDs(itemStackIDs);
            dropTable.setProbabilities(probabilities);

            println(getClass(), "DropTableID: " + dropTableID, false, PRINT_DEBUG);
            //println(getClass(), "ItemStackID: " + itemID, false, PRINT_DEBUG);
            println(PRINT_DEBUG);

            dropTables.add(dropTable);
        }

        println(getClass(), "====== END LOADING DROP TABLES ======", false, PRINT_DEBUG);
        return dropTables;
    }

    @Setter
    @Getter
    public class DropTable {
        private int[] itemStackIDs;
        private float[] probabilities;

        private final int dropTableID;

        DropTable(int dropTableID) {
            this.dropTableID = dropTableID;
        }
    }
}
