package com.forgestorm.server.io;

import com.forgestorm.server.game.abilities.Ability;
import com.forgestorm.server.game.abilities.AbilityType;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class AbilityLoader {

    private static final boolean PRINT_DEBUG = false;

    public Map<Short, Ability> loadCombatAbilities() {
        println(getClass(), "====== START LOADING ABILITIES ======", false, PRINT_DEBUG);

        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(FilePaths.COMBAT_ABILITIES.getFilePath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<Integer, Map<String, Object>> root = yaml.load(inputStream);

        Map<Short, Ability> combatAbilities = new HashMap<>();
        for (Map.Entry<Integer, Map<String, Object>> entry : root.entrySet()) {
            int abilityId = entry.getKey();
            Map<String, Object> itemNode = entry.getValue();

            Ability ability = new Ability();

            /*
             * Get universal item information
             */
            String name = (String) itemNode.get("name");
            AbilityType abilityType = AbilityType.valueOf((String) itemNode.get("abilityType"));
//            Short animation = (Short) itemNode.get("animation");
            Integer damageMin = (Integer) itemNode.get("damageMin");
            Integer damageMax = (Integer) itemNode.get("damageMax");
            Integer cooldown = (Integer) itemNode.get("cooldown");
            Integer distanceMin = (Integer) itemNode.get("distanceMin");
            Integer distanceMax = (Integer) itemNode.get("distanceMax");

            ability.setAbilityId((short) abilityId);
            ability.setName(name);
            ability.setAbilityType(abilityType);
//            ability.setAbilityAnimation(animation);
            ability.setDamageMin(damageMin);
            ability.setDamageMax(damageMax);
            ability.setCooldown(cooldown);
            ability.setDistanceMin(distanceMin);
            ability.setDistanceMax(distanceMax);

            combatAbilities.put((short) abilityId, ability);

            println(PRINT_DEBUG);

        }

        println(getClass(), "====== END LOADING ABILITIES ======", false, PRINT_DEBUG);
        return combatAbilities;
    }
}
