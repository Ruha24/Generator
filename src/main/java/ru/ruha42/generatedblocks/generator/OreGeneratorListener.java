package ru.ruha42.generatedblocks.generator;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.ruha42.generatedblocks.Main;
import ru.ruha42.generatedblocks.data.ConfigData;
import ru.ruha42.generatedblocks.data.OreData;
import ru.ruha42.generatedblocks.menu.MenuUpgrade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OreGeneratorListener implements Listener {

    public OreGeneratorListener() {
        new BukkitRunnable() {
            @Override
            public void run() {
                generatedOre();
            }
        }.runTaskTimer(Main.main, 20, 1);
    }

    private List<OreData> oreDataList = ConfigData.getOres();

    public static Map<OreGenerator, Long> oreDataSpeedGeneration = new HashMap<>();

    public static final Material mainBlock = Material.COMMAND_BLOCK;

    private final MenuUpgrade menu = new MenuUpgrade();

    private final Random random = new Random();

    private static final NamespacedKey keyBlock = NamespacedKey.minecraft("block_data");
    private static final NamespacedKey keyBlockFilter = NamespacedKey.minecraft("block_filter");

    public static final Map<Location, OreGenerator> oreGeneratorMap = new HashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var block = event.getBlock();
        var loc = block.getLocation();
        var player = event.getPlayer();

        var oreGenerator = oreGeneratorMap.get(loc.add(0, 1, 0));
        Material blockType = block.getType();

        if (block.getType() == mainBlock) {
            event.setCancelled(true);
        } else if (oreGenerator != null) {
            if (oreGenerator.getMaterialBlocks().contains(blockType.toString())) {
                event.setDropItems(false);
            } else if (oreGenerator.getLevel(24) == 2 && isSmeltableOre(blockType)) {
                Material smeltedDrop = dropSmeltedOre(blockType);

                if (smeltedDrop != null) {
                    event.setDropItems(false);

                    ItemStack tool = player.getInventory().getItemInMainHand();
                    int fortuneLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);

                    int amount = calculateFortuneDrop(fortuneLevel);

                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smeltedDrop, amount));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        var block = event.getClickedBlock();

        var itemInHand = event.getPlayer().getInventory().getItemInMainHand();

        if (block != null && itemInHand.getType() == mainBlock) {
            var blockUp = block.getLocation().getBlock().getRelative(BlockFace.UP);

            blockUp.setType(mainBlock);

            var persist = event.getItem().getPersistentDataContainer();

            String data = "";
            String filterData = "";

            if (persist.has(keyBlock))
                data = persist.get(keyBlock, PersistentDataType.STRING);

            if (persist.has(keyBlockFilter))
                filterData = persist.get(keyBlockFilter, PersistentDataType.STRING);

            OreGenerator oreGenerator = new OreGenerator(blockUp, data, filterData);

            oreGenerator.saveLevels();

            oreGeneratorMap.put(blockUp.getLocation(), oreGenerator);

            itemInHand.setAmount(itemInHand.getAmount() - 1);
        }
    }

    @EventHandler
    public void onBlockClick(PlayerInteractEvent event) {
        var block = event.getClickedBlock();
        if (block != null && block.getType() == mainBlock) {

            Player player = event.getPlayer();

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                OreGenerator oreGenerator = oreGeneratorMap.get(block.getLocation());

                if (oreGenerator != null) {
                    menu.showMenu(player, oreGenerator);
                }
            }
        }
    }

    private int calculateFortuneDrop(int fortuneLevel) {
        Random random = new Random();
        int amount = 1;

        if (fortuneLevel > 0) {
            for (int i = 0; i < fortuneLevel; i++) {
                if (random.nextFloat() < (fortuneLevel / 3.0)) {
                    amount++;
                }
            }
        }
        return amount;
    }

    private void generatedOre() {
        oreGeneratorMap.forEach((location, oreGenerator) -> {
            if (oreDataSpeedGeneration.containsKey(oreGenerator) &&
                    System.currentTimeMillis() <= oreDataSpeedGeneration.get(oreGenerator)) return;

            oreDataSpeedGeneration.put(oreGenerator, System.currentTimeMillis() + oreGenerator.getSpeed() * 1000L);

            Block block = oreGenerator.getBlock();

            Block underBlock = block.getRelative(0, -1, 0);

            if (underBlock.isEmpty()) {
                Material newOre = getRandomOre(oreGenerator.getLevel(24));

                if (!oreGenerator.getMaterialBlocks().contains(newOre))
                    underBlock.setType(newOre, false);

                if (oreGenerator.getLevel(44) == 1)
                    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1, 2);

                block.getWorld().spawnParticle(Particle.BLOCK, block.getLocation(), 10, newOre.createBlockData());
            }
        });
    }

    private boolean isSmeltableOre(Material ore) {
        return ore == Material.IRON_ORE || ore == Material.GOLD_ORE || ore == Material.COPPER_ORE;
    }

    private Material getRandomOre(int levelChanceOre) {

        double totalChance = oreDataList.stream()
                .mapToDouble(ore -> ore.getOreChance(levelChanceOre))
                .sum();

        double randomValue = random.nextDouble() * totalChance;

        for (OreData oreData : oreDataList) {
            randomValue -= oreData.getOreChance(levelChanceOre);
            if (randomValue <= 0) {
                try {
                    return Material.valueOf(oreData.getOreName());
                } catch (IllegalArgumentException e) {
                    return Material.STONE;
                }
            }
        }

        return Material.STONE;
    }

    public static void removeOreGenerator(Location loc) {
        oreGeneratorMap.remove(loc);
        loc.getWorld().getPersistentDataContainer().remove(new NamespacedKey("ruha42", "block_%d_%d_%d".formatted(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())));
    }

    private Material dropSmeltedOre(Material ore) {
        Material smeltedMaterial;

        switch (ore) {
            case IRON_ORE:
                smeltedMaterial = Material.IRON_INGOT;
                break;
            case GOLD_ORE:
                smeltedMaterial = Material.GOLD_INGOT;
                break;
            case COPPER_ORE:
                smeltedMaterial = Material.COPPER_INGOT;
                break;
            default:
                smeltedMaterial = Material.STONE;
                break;
        }

        return smeltedMaterial;
    }

}
