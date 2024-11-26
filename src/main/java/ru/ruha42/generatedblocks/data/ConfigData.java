package ru.ruha42.generatedblocks.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import ru.ruha42.generatedblocks.Main;
import ru.ruha42.generatedblocks.upgrades.Upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigData {

    static FileConfiguration config = Main.main.getConfig();

    public static List<Upgrade> getUpgrades() {

        List<Upgrade> upgrades = new ArrayList<>();

        for (String key : config.getConfigurationSection("upgrades").getKeys(false)) {
            Map<Integer, Integer> levels = new HashMap<>();
            Map<Integer, Integer> speeds = new HashMap<>();

            ItemStack info = config.getItemStack("upgrades." + key + ".item");

            ConfigurationSection levelSection = config.getConfigurationSection("upgrades." + key + ".levels");

            if (levelSection != null) {
                for (String levelKey : levelSection.getKeys(false)) {
                    int level = Integer.parseInt(levelKey);
                    int value = levelSection.getInt(levelKey);
                    levels.put(level, value);
                }
            }

            ConfigurationSection speedSection = config.getConfigurationSection("upgrades." + key + ".speedSpawn");

            if (speedSection != null) {
                for (String levelKey : speedSection.getKeys(false)) {
                    int level = Integer.parseInt(levelKey);
                    int value = speedSection.getInt(levelKey);
                    speeds.put(level, value);
                }
            }

            Upgrade upgrade = new Upgrade(Integer.valueOf(key), info, levels, speeds);
            upgrades.add(upgrade);
        }

        return upgrades;
    }

    public static List<OreData> getOres() {

        List<OreData> ores = new ArrayList<>();

        for (String key : config.getConfigurationSection("ores").getKeys(false)) {
            OreData ore = config.getSerializable("ores." + key, OreData.class);

            if (ore == null) continue;
            ore.setOreName(key);
            ores.add(ore);
        }

        return ores;
    }


}
