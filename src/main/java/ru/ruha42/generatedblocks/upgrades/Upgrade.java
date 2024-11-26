package ru.ruha42.generatedblocks.upgrades;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

@Getter
public class Upgrade {

    private String name;
    private String lore;
    private int slot;
    private Material material;
    private Map<Integer, Integer> levels;
    private Map<Integer, Integer> speedOre;

    public Upgrade(int slot, ItemStack item, Map<Integer, Integer> levels, Map<Integer, Integer> speedOre) {
        material = item.getType();
        this.slot = slot;
        this.levels = levels;
        this.speedOre = speedOre;

        var meta = item.getItemMeta();

        name = meta.getDisplayName();
        this.lore = meta.hasLore() ? String.join(", ", meta.getLore()) : "Описание отсутствует";
    }

    public int getPrice(int level) {

        Integer price = levels.get(level);

        if (price != null) {
            return price;
        }

        int maxLevel = levels.keySet().stream().max(Integer::compare).orElse(1);

        return levels.getOrDefault(maxLevel, 1);
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
