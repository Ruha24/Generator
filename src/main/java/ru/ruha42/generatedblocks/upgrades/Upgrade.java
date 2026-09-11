package ru.ruha42.generatedblocks.upgrades;

import lombok.Getter;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

@Getter
public class Upgrade {

    private String name;
    private String lore;
    private int slot;
    private Material material;
    private Map<Integer, Integer> levels;
    private Map<Integer, Integer> speedOre;

    public Upgrade(int slot, Material material, String name, List<String> lore, Map<Integer, Integer> levels, Map<Integer, Integer> speedOre) {
        this.slot = slot;
        this.material = material;
        this.name = name;
        this.levels = levels;
        this.speedOre = speedOre;
        this.lore = (lore == null || lore.isEmpty()) ? "Описание отсутствует" : String.join(", ", lore);
    }

    public int getPrice(int level) {

        Integer price = levels.get(level);

        if (price != null) {
            return price;
        }

        int maxLevel = levels.keySet().stream().max(Integer::compare).orElse(1);

        return levels.getOrDefault(maxLevel, 1);
    }

    public int getMaxLevel() {
        return levels.keySet().stream().max(Integer::compare).orElse(1) + 1;
    }

    public int getSpeedOre(int level) {
        Integer speed = speedOre.get(level);

        if (speed != null) {
            return speed;
        }

        int maxSpeed = speedOre.keySet().stream().max(Integer::compare).orElse(20);

        return speedOre.getOrDefault(maxSpeed, 20);
    }
}
