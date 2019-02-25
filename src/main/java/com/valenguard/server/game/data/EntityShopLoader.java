package com.valenguard.server.game.data;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class EntityShopLoader {

    private static final String FILE_PATH = "src/main/resources/data/item/ShopItems.yaml";

    @SuppressWarnings("unchecked")
    public static Map<Short, List<Integer>> loadFromFile() {

        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(FILE_PATH));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Iterable<Object> iterable = yaml.loadAll(inputStream);

        Map<Short, List<Integer>> map = null;

        for (Object object : iterable) map = (Map<Short, List<Integer>>) object;
        return map;
    }
}
