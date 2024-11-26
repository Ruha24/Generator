package ru.ruha42.generatedblocks.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Data
public class OreData implements ConfigurationSerializable {

    private String oreName;

    private Map<Integer, Double> oreLevels;

    private int oreLevel;

    private double oreChance;

    public OreData(Map<String, Object> ore) {
        this.oreLevels = new HashMap<>();
        Map<?, ?> rawLevels = (Map<?, ?>) ore.get("levels");

        for (Map.Entry<?, ?> entry : rawLevels.entrySet()) {
            int key = (int) entry.getKey();
            double value = (double) entry.getValue();

            this.oreLevels.put(key, value);
        }

        this.oreLevel = oreLevels.keySet().iterator().next();
        this.oreChance = oreLevels.get(oreLevel);
    }

    public double getOreChance(int oreLevel) {
        return oreLevels.get(oreLevel);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of();
    }
}
