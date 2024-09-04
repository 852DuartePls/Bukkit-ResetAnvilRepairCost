package me.duart.resetAnvilRepairCost;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.java.JavaPlugin;

public final class ResetAnvilRepairCost extends JavaPlugin implements Listener {
    private int MAX_REPAIR_COST;
    private int ADJUSTED_COST;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("Plugin enabled!");
        MAX_REPAIR_COST = getConfig().getInt("value.max_repair_cost", 35);
        ADJUSTED_COST = getConfig().getInt("value.adjusted_cost", 20);
        getServer().getPluginManager().registerEvents(this, this);
    }

    // Everytime an item surpasses the MAX_REPAIR_COST, it will be set to the ADJUSTED_COST.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack item = event.getResult();
        if (item == null || item.getItemMeta() == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Repairable repairable) {
            int repairCost = repairable.getRepairCost();
            if (repairCost >= MAX_REPAIR_COST) {
                repairable.setRepairCost(ADJUSTED_COST);
                item.setItemMeta(meta);
                event.setResult(item);
            }
        }
    }

    // If an item is already with a repair cost of 40 or higher, this will be set to the ADJUSTED_COST,
    // when clicking on the item again.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL) return;
        if (event.getRawSlot() != 0) return;
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Repairable repairable) {
            int repairCost = repairable.getRepairCost();
            if (repairCost >= 40) {
                repairable.setRepairCost(ADJUSTED_COST);
                item.setItemMeta(meta);
                event.getInventory().setItem(event.getSlot(), item);
            }
        }
    }
}