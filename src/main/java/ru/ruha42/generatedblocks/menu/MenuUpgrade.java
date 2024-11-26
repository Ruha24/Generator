package ru.ruha42.generatedblocks.menu;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.ruha42.generatedblocks.Main;
import ru.ruha42.generatedblocks.data.ConfigData;
import ru.ruha42.generatedblocks.generator.OreGenerator;
import ru.ruha42.generatedblocks.generator.OreGeneratorListener;
import ru.ruha42.generatedblocks.upgrades.Upgrade;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.Arrays;
import java.util.Map;

public class MenuUpgrade {

    private Window.Builder windowBuilder;

    private final Gui gui;

    private OreGenerator oreGenerator;

    public MenuUpgrade() {
        SimpleItem collector = new SimpleItem(new ItemBuilder(OreGeneratorListener.mainBlock).setDisplayName("Собрать генератор"),
                click -> collectOnInventory(click.getPlayer()));

        SimpleItem filter = new SimpleItem(new ItemBuilder(Material.HOPPER).setDisplayName("Фильтр"), click -> openFilterMenu(click.getPlayer()));

        gui = PagedGui.items()
                .setStructure(
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . z . x . .")
                .addIngredient('z', collector)
                .addIngredient('x', filter)
                .build();
    }

    public void showMenu(Player player, OreGenerator oreGenerator) {
        this.oreGenerator = oreGenerator;

        for (Upgrade upgrade : ConfigData.getUpgrades()) {
            int slot = upgrade.getSlot();

            if (Arrays.stream(OreGenerator.upgradeSlots).anyMatch(s -> s == slot)) {
                int currentLevel = oreGenerator.getLevel(slot);
                gui.setItem(slot, new SimpleItem(
                        new ItemBuilder(upgrade.getMaterial())
                                .setDisplayName(slot == 24
                                        ? (currentLevel > 1 ? upgrade.getName() + " Есть" : upgrade.getName() + " Нету")
                                        : upgrade.getName() + " " + currentLevel)
                                .addLoreLines(upgrade.getLore()),
                        click -> {
                            addLevel(click.getPlayer(), slot, currentLevel, oreGenerator, upgrade.getPrice(currentLevel));

                            if(slot == 22 && upgrade.getSpeedOre() != null) {
                                oreGenerator.setSpeed(upgrade.getSpeedOre(oreGenerator.getLevel(22)));
                            }

                            showMenu(player, oreGenerator);
                        }
                ));
            }
        }

        int sound = oreGenerator.getLevel(44);

        SimpleItem onSound = new SimpleItem(new ItemBuilder(Material.BARRIER)
                .setDisplayName(sound == 1 ? "Звук включён" : "Звук выключен"), click -> {
                addLevel(click.getPlayer(), 44, sound, oreGenerator, 0);
                showMenu(player, oreGenerator);
        }  );

        gui.setItem(44, onSound);

        windowBuilder = Window.single()
                .setGui(gui)
                .setTitle("&e⛏ &#FF9100&lШахтёр");

        windowBuilder.open(player);
    }

    private void collectOnInventory(Player player) {

        Block block = oreGenerator.getBlock();
        block.setType(Material.AIR);

        ItemStack generatorItem = new ItemStack(OreGeneratorListener.mainBlock);
        ItemMeta meta = generatorItem.getItemMeta();
        meta.setDisplayName("Генератор");
        meta.setLore(oreGenerator.getLore());
        meta.getPersistentDataContainer().set(NamespacedKey.minecraft("block_data"), PersistentDataType.STRING, OreGenerator.getGson().toJson(oreGenerator.getSlotLevels()));
        meta.getPersistentDataContainer().set(NamespacedKey.minecraft("block_filter"), PersistentDataType.STRING, OreGenerator.getGson().toJson(oreGenerator.getMaterialBlocks()));
        generatorItem.setItemMeta(meta);

        Map<Integer, ItemStack> leftover = player.getInventory().addItem(generatorItem);
        if (!leftover.isEmpty()) {
            block.getWorld().dropItem(block.getLocation(), generatorItem);
        }

        player.closeInventory();

        OreGeneratorListener.removeOreGenerator(block.getLocation());
    }

    private void openFilterMenu(Player player) {
        MenuFilter menuFilter = new MenuFilter();
        menuFilter.openFilterMenu(player, oreGenerator);
    }

    private void addLevel(Player player, int slot, int level, OreGenerator oreGenerator, int price) {
        int newLevel = level + 1;

        switch (slot) {
            case 20:
            case 22:
                if (level >= 5) {
                    player.sendMessage("Это улучшение максимального уровня");
                    return;
                }
                if (!buyUpgrade(player, price)) {
                    return;
                }

                player.sendMessage("Вы успешно улучшили уровень до " + newLevel);
                break;

            case 24:
                if (level >= 2) {
                    player.sendMessage("Это улучшение максимального уровня");
                    return;
                }
                if (!buyUpgrade(player, price)) {
                    return;
                }

                player.sendMessage("Вы успешно улучшили уровень до " + newLevel);
                break;

            case 44:
                newLevel = (level == 1) ? 0 : 1;
                break;

            default:
                return;
        }

        oreGenerator.setLevel(slot, newLevel);
    }

    private boolean buyUpgrade(Player player, Integer price) {
        if (Main.economy.getBalance(player) < price) {
            player.sendMessage("У вас недостаточно денег: " + (price - Main.economy.getBalance(player)) + " $");
            return false;
        }

        Main.economy.withdrawPlayer(player, price);
        player.sendMessage("Вы успешно купили улучшение за " + price + " $");
        return true;
    }
}
