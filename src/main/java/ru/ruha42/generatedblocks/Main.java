package ru.ruha42.generatedblocks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.ruha42.generatedblocks.data.ConfigData;
import ru.ruha42.generatedblocks.data.OreData;
import ru.ruha42.generatedblocks.generator.OreGenerator;
import ru.ruha42.generatedblocks.generator.OreGeneratorListener;
import ru.ruha42.generatedblocks.menu.MenuFilterListener;
import ru.ruha42.generatedblocks.upgrades.Upgrade;
import xyz.xenondevs.invui.InvUI;

import java.util.List;
import java.util.Optional;

public final class Main extends JavaPlugin {

    public static Main main;

    public static Economy economy;

    @Override
    public void onEnable() {
        main = this;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        economy = rsp.getProvider();

        ConfigurationSerialization.registerClass(OreData.class);
        saveDefaultConfig();

        List<Upgrade> upgrades = ConfigData.getUpgrades();

        Bukkit.getWorlds().forEach(world -> {
            var persist = world.getPersistentDataContainer();

            persist.getKeys()
                    .stream()
                    .filter(namespacedKey ->  namespacedKey.getNamespace().equals("ruha42"))
                    .forEach(namespacedKey -> {
                        String[] locationData = namespacedKey.getKey().split("_");
                        int x = Integer.parseInt(locationData[1]);
                        int y = Integer.parseInt(locationData[2]);
                        int z = Integer.parseInt(locationData[3]);
                        Location loc = new Location(world, x, y, z);
                        Block block = loc.getBlock();
                        String key = persist.get(new NamespacedKey("ruha42_filter", "block_%d_%d_%d_filter".formatted(block.getX(), block.getY(), block.getZ())), PersistentDataType.STRING);
                        OreGenerator oreGenerator = new OreGenerator(block, persist.get(namespacedKey, PersistentDataType.STRING), key);

                        int level = oreGenerator.getLevel(22);

                        Optional<Upgrade> optionalUpgrade = upgrades.stream()
                                .filter(upgrade -> upgrade.getSpeedOre() != null)
                                .findFirst();

                        if (optionalUpgrade.isPresent()) {
                            Upgrade upgrade = optionalUpgrade.get();
                            int speed = upgrade.getSpeedOre(level);

                            oreGenerator.setSpeed(speed);
                        }

                        OreGeneratorListener.oreGeneratorMap.put(loc, oreGenerator);
                    });
        });

        InvUI.getInstance().setPlugin(this);

        getServer().getPluginManager().registerEvents(new MenuFilterListener(), this);
        getServer().getPluginManager().registerEvents(new OreGeneratorListener(), this);
    }

    @Override
    public void onDisable() {
    }
}
