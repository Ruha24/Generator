package ru.ruha42.generatedblocks.menu;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.ruha42.generatedblocks.generator.OreGenerator;

import java.util.List;

public class MenuFilter implements Listener, InventoryHolder {

    @Getter
    private OreGenerator oreGenerator;

    private final Inventory inventory;

    public MenuFilter() {
         inventory = Bukkit.createInventory(this, InventoryType.HOPPER, "Menu Filter");
    }

    public void openFilterMenu(Player player, OreGenerator oreGenerator) {
        this.oreGenerator = oreGenerator;

        if (oreGenerator.getMaterialBlocks() != null) {
            List<String> materialBlocks = oreGenerator.getMaterialBlocks();
            for (int i = 0; i < materialBlocks.size() && i < inventory.getSize(); i++) {
                Material material = Material.valueOf(materialBlocks.get(i));
                ItemStack item = new ItemStack(material);
                inventory.setItem(i, item);
            }
        } else {
            inventory.clear();
        }

        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
