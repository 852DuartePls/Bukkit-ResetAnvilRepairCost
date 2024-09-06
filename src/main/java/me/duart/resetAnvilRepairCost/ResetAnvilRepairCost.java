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

import java.util.Objects;

public final class ResetAnvilRepairCost extends JavaPlugin implements Listener {
    ConsoleCommandSender console;
    private final String PluginName = this.getName();

    private int MAX_REPAIR_COST;
    private int ADJUSTED_COST;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        this.MAX_REPAIR_COST = config.getInt("value.max_repair_cost", 35);
        this.ADJUSTED_COST = config.getInt("value.adjusted_cost", 20);

        console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.YELLOW + PluginName + " || enabled!");
        console.sendMessage(ChatColor.YELLOW + PluginName + " || Max Repair Cost: " + ChatColor.GREEN + MAX_REPAIR_COST);
        console.sendMessage(ChatColor.YELLOW + PluginName + " || Adjusted Cost: " + ChatColor.GREEN + ADJUSTED_COST);
        Objects.requireNonNull(getCommand("resetrepaircost")).setExecutor(new ReloadCommand(this));
        Objects.requireNonNull(getCommand("resetrepaircost")).setTabCompleter(new ReloadCommand(this));
        getServer().getPluginManager().registerEvents(this, this);
    }

    // Reloads the plugin configuration.
    public void onReload() {
        reloadConfig();
        FileConfiguration config = getConfig();

        this.MAX_REPAIR_COST = config.getInt("value.max_repair_cost", 35);
        this.ADJUSTED_COST = config.getInt("value.adjusted_cost", 20);
        console.sendMessage(ChatColor.YELLOW + PluginName + "|| Modified Max Repair Cost set to: " + ChatColor.GREEN + MAX_REPAIR_COST);
        console.sendMessage(ChatColor.YELLOW + PluginName + "|| Modified Adjusted Cost set to: " + ChatColor.GREEN + ADJUSTED_COST);

    }

    // Everytime an item surpasses the MAX_REPAIR_COST, it will be set to the ADJUSTED_COST.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack item = event.getResult();
        if (item == null || item.getItemMeta() == null) return;
        if (noPermission((Player) event.getView().getPlayer())) return;

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

    // If an item is already with a repair cost that is >= MAX_REPAIR_COST,
    // this will be set to the ADJUSTED_COST, when clicking on the item again.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL) return;
        if (event.getRawSlot() != 0) return;
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;
        if (noPermission((Player) event.getWhoClicked())) return;

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Repairable repairable) {
            int repairCost = repairable.getRepairCost();
            if (repairCost >= MAX_REPAIR_COST) {
                repairable.setRepairCost(ADJUSTED_COST);
                item.setItemMeta(meta);
                event.getInventory().setItem(event.getSlot(), item);
            }
        }
    }

    // Permission check.
    private boolean noPermission(Player player) {
        return !player.hasPermission("resetrepaircost.user");
    }
}