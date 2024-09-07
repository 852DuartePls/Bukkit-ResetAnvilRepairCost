package me.duart.resetAnvilRepairCost;

import me.duart.resetAnvilRepairCost.command.ReloadCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ResetAnvilRepairCost extends JavaPlugin implements Listener {
    ConsoleCommandSender console;
    private final String PluginName = this.getName();

    private final Map<String, Integer> configCache = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();
        console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.YELLOW + PluginName + " || " + "Enabled!");
        console.sendMessage(ChatColor.YELLOW + PluginName + " || "
                +"Max Repair Cost: " + ChatColor.GREEN + configCache.get("max_repair_cost"));
        console.sendMessage(ChatColor.YELLOW + PluginName + " || "
                +"Adjusted Cost: " + ChatColor.GREEN + configCache.get("adjusted_cost"));
        Objects.requireNonNull(getCommand("anvilresetcost")).setExecutor(new ReloadCommand(this));
        Objects.requireNonNull(getCommand("anvilresetcost")).setTabCompleter(new ReloadCommand(this));
        getServer().getPluginManager().registerEvents(this, this);
    }

    // Reloads the plugin configuration.
    public void onReload() {
        reloadConfig();
        loadConfigValues();
        console.sendMessage(ChatColor.YELLOW + PluginName + " || "
                +"Modified Max Repair Cost set to: " + ChatColor.GREEN + configCache.get("max_repair_cost"));
        console.sendMessage(ChatColor.YELLOW + PluginName + " || "
                +"Modified Adjusted Cost set to: " + ChatColor.GREEN + configCache.get("adjusted_cost"));
    }

    private void loadConfigValues() {
        FileConfiguration config = getConfig();
        configCache.put("max_repair_cost", config.getInt("value.max_repair_cost", 35));
        configCache.put("adjusted_cost", config.getInt("value.adjusted_cost", 20));
    }

    // Every time an item surpasses the MAX_REPAIR_COST, it will be set to the ADJUSTED_COST.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack item = event.getResult();
        if (item == null || item.getItemMeta() == null) return;
        if (hasPlayerPermission((Player) event.getView().getPlayer())) return;

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Repairable repairable) {
            int repairCost = repairable.getRepairCost();
            int maxRepairCost = configCache.get("max_repair_cost");
            int adjustedCost = configCache.get("adjusted_cost");

            if (repairCost >= maxRepairCost) {
                repairable.setRepairCost(adjustedCost);
                item.setItemMeta(meta);
                event.setResult(item);
            }
        }
    }

    // If an item is already with a repair cost that is >= MAX_REPAIR_COST,
    // this will be set to the ADJUSTED_COST when clicking on the item again.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL) return;
        if (event.getRawSlot() != 0) return;
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;
        if (hasPlayerPermission((Player) event.getWhoClicked())) return;

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Repairable repairable) {
            int repairCost = repairable.getRepairCost();
            int maxRepairCost = configCache.get("max_repair_cost");
            int adjustedCost = configCache.get("adjusted_cost");

            if (repairCost >= maxRepairCost) {
                repairable.setRepairCost(adjustedCost);
                item.setItemMeta(meta);
                event.getInventory().setItem(event.getSlot(), item);
            }
        }
    }

    // Permission check.
    private boolean hasPlayerPermission(Player player) {
        return !player.hasPermission("resetrepaircost.user");
    }
}