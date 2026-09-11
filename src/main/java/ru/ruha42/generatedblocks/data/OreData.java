package ru.ruha42.generatedblocks.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class OreData {

    private String oreName;

    private Map<Integer, Double> oreLevels;

    public double getOreChance(int oreLevel) {
        Double chance = oreLevels.get(oreLevel);

        if (chance != null) {
            return chance;
        }

        int maxLevel = oreLevels.keySet().stream().max(Integer::compare).orElse(1);

        return oreLevels.getOrDefault(maxLevel, 0.0);
    }
}
