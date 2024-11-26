package ru.ruha42.generatedblocks.generator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ru.ruha42.generatedblocks.data.ConfigData;
import ru.ruha42.generatedblocks.upgrades.Upgrade;

import java.lang.reflect.Type;
import java.util.*;


public class OreGenerator {

    private final PersistentDataContainer container;
    private final NamespacedKey upgradeKey;
    private final NamespacedKey filterKey;

    @Getter @Setter
    private int speed = 1;

    @Getter
    private static final Gson gson = new Gson();

    @Getter
    private Map<Integer, Integer> slotLevels = new HashMap<>();
    @Getter
    private Block block;

    public static final int[] upgradeSlots = {20, 22, 24, 44};

    @Getter
    private List<String> materialBlocks = new ArrayList<>();

    public OreGenerator(Block block, String data, String filterData) {
        this.block = block;
        this.container = block.getWorld().getPersistentDataContainer();

        String formattedKey = String.format("block_%d_%d_%d", block.getX(), block.getY(), block.getZ());
        String formattedFilter = String.format("block_%d_%d_%d_filter", block.getX(), block.getY(), block.getZ());
        upgradeKey = new NamespacedKey("ruha42", formattedKey);
        filterKey = new NamespacedKey("ruha42_filter", formattedFilter);

        if(filterData != null && !filterData.isEmpty())
            loadFilter(filterData);

        if (!data.isEmpty()) {
            loadLevels(data);
        } else {
            initializeLevels();
        }
    }

    public List<String> getLore() {
        List<String> lore = new ArrayList<>();

        List<Upgrade> upgrades = ConfigData.getUpgrades();

        for (Upgrade upgrade : upgrades) {
            int slot = upgrade.getSlot();
            int level = getLevel(slot);

            if (slot == 24) {
                lore.add(upgrade.getName() + ": " + (level > 1 ? "Есть" : "Нету"));
            } else {
                lore.add(upgrade.getName() + ": " + level + " уровень");
            }
        }

        return lore;
    }


    private void initializeLevels() {
        for (int slot : upgradeSlots) {
            slotLevels.put(slot, 1);
        }
    }

    private void loadFilter(String data) {
        Type type = new TypeToken<List<String>>() {}.getType();
        materialBlocks = gson.fromJson(data, type);
    }

    private void loadLevels(String data) {
        Type type = new TypeToken<Map<Integer, Integer>>() {}.getType();
        slotLevels = gson.fromJson(data, type);
    }

    public int getLevel(int slot) {
        return slotLevels.getOrDefault(slot, 1);
    }

    public void setLevel(int slot, int level) {
        if (Arrays.stream(upgradeSlots).anyMatch(s -> s == slot)) {
            slotLevels.put(slot, level);
            saveLevels();
        }
    }

    public void saveFilter() {
        String json = gson.toJson(materialBlocks);
        container.set(filterKey, PersistentDataType.STRING, json);
    }

    public void saveLevels() {
        String json = gson.toJson(slotLevels);
        container.set(upgradeKey, PersistentDataType.STRING, json);
    }
}

