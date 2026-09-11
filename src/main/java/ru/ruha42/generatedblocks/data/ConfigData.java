package ru.ruha42.generatedblocks.data;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.ruha42.generatedblocks.Main;
import ru.ruha42.generatedblocks.upgrades.Upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfigData {

    static FileConfiguration config = Main.main.getConfig();

    public static List<Upgrade> getUpgrades() {

        List<Upgrade> upgrades = new ArrayList<>();

        ConfigurationSection upgradesSection = config.getConfigurationSection("upgrades");

        if (upgradesSection == null) {
            Main.main.getLogger().warning("config.yml: missing 'upgrades' section, no upgrades will be available");
            return upgrades;
        }

        for (String key : upgradesSection.getKeys(false)) {
            int slot;

            try {
                slot = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                Main.main.getLogger().warning("config.yml: upgrade key '" + key + "' is not a valid slot number, skipping");
                continue;
            }

            String materialName = config.getString("upgrades." + key + ".material");
            Material material = materialName != null ? Material.matchMaterial(materialName) : null;

            if (material == null) {
                Main.main.getLogger().warning("config.yml: upgrade '" + key + "' has no valid 'material', skipping");
                continue;
            }

            String name = config.getString("upgrades." + key + ".name", key);
            List<String> lore = config.getStringList("upgrades." + key + ".lore");

            Map<Integer, Integer> levels = new HashMap<>();
            Map<Integer, Integer> speeds = new HashMap<>();

            ConfigurationSection levelSection = config.getConfigurationSection("upgrades." + key + ".levels");

            if (levelSection != null) {
                for (String levelKey : levelSection.getKeys(false)) {
                    parseLevelKey(levelKey, "upgrades." + key + ".levels")
                            .ifPresent(level -> levels.put(level, levelSection.getInt(levelKey)));
                }
            }

            if (levels.isEmpty()) {
                Main.main.getLogger().warning("config.yml: upgrade '" + key + "' has no valid 'levels', skipping");
                continue;
            }

            ConfigurationSection speedSection = config.getConfigurationSection("upgrades." + key + ".speedSpawn");

            if (speedSection != null) {
                for (String levelKey : speedSection.getKeys(false)) {
                    parseLevelKey(levelKey, "upgrades." + key + ".speedSpawn")
                            .ifPresent(level -> speeds.put(level, speedSection.getInt(levelKey)));
                }
            }

            upgrades.add(new Upgrade(slot, material, name, lore, levels, speeds));
        }

        return upgrades;
    }

    public static List<OreData> getOres() {

        List<OreData> ores = new ArrayList<>();

        ConfigurationSection oresSection = config.getConfigurationSection("ores");

        if (oresSection == null) {
            Main.main.getLogger().warning("config.yml: missing 'ores' section, no ore will ever generate");
            return ores;
        }

        for (String key : oresSection.getKeys(false)) {
            Map<Integer, Double> levels = new HashMap<>();

            ConfigurationSection levelSection = config.getConfigurationSection("ores." + key + ".levels");

            if (levelSection != null) {
                for (String levelKey : levelSection.getKeys(false)) {
                    parseLevelKey(levelKey, "ores." + key + ".levels")
                            .ifPresent(level -> levels.put(level, levelSection.getDouble(levelKey)));
                }
            }

            if (levels.isEmpty()) {
                Main.main.getLogger().warning("config.yml: ore '" + key + "' has no valid 'levels', skipping");
                continue;
            }

            ores.add(new OreData(key, levels));
        }

        return ores;
    }

    private static Optional<Integer> parseLevelKey(String levelKey, String path) {
        try {
            return Optional.of(Integer.parseInt(levelKey));
        } catch (NumberFormatException e) {
            Main.main.getLogger().warning("config.yml: '" + path + "." + levelKey + "' is not a valid level number, skipping");
            return Optional.empty();
        }
    }


}
