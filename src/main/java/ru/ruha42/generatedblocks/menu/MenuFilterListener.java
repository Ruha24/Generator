package ru.ruha42.generatedblocks.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MenuFilterListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        var holder = event.getInventory().getHolder();

        if (holder instanceof MenuFilter menuFilter) {
            menuFilter.getOreGenerator().getMaterialBlocks().clear();
            Inventory inventory = event.getInventory();

            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType().isBlock()) {
                    menuFilter.getOreGenerator().getMaterialBlocks().add(item.getType().toString());
                }
            }

            menuFilter.getOreGenerator().saveFilter();
        }
    }

}
