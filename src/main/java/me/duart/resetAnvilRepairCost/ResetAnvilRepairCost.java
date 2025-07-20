package me.duart.resetAnvilRepairCost;

import me.duart.resetAnvilRepairCost.command.ReloadCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.java.JavaPlugin;

public final class ResetAnvilRepairCost extends JavaPlugin implements Listener {

    private int maxRepairCost;
    private int adjustedCost;
    private boolean enabled;
    private boolean checkPermission;

    ConsoleCommandSender console;
    private final String PluginName = this.getName();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.YELLOW + PluginName + " || Enabled!");
        console.sendMessage(ChatColor.YELLOW + PluginName + " || Max: " + maxRepairCost);
        console.sendMessage(ChatColor.YELLOW + PluginName + " || Adjusted: " + adjustedCost);
        PluginCommand command = getCommand("anvilresetcost");
        if (command != null) {
            command.setExecutor(new ReloadCommand(this));
            command.setTabCompleter(new ReloadCommand(this));
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    // Reloads the plugin from the configuration file.
    public void onReload() {
        reloadConfig();
        loadConfig();
        console.sendMessage(ChatColor.YELLOW + PluginName + " || Enabled: " + enabled);
        console.sendMessage(ChatColor.YELLOW + PluginName + " || Bypass Permission: " + checkPermission);
        console.sendMessage(ChatColor.YELLOW + PluginName + " || Max: " + maxRepairCost);
        console.sendMessage(ChatColor.YELLOW + PluginName + " || Adjusted: " + adjustedCost);
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        maxRepairCost   = config.getInt("value.max-repair-cost", 35);
        adjustedCost    = config.getInt("value.adjusted-cost", 20);
        enabled         = config.getBoolean("value.enabled", true);
        checkPermission = config.getBoolean("value.bypass-permission", true);
    }

    // Every time an item surpasses the MAX_REPAIR_COST, it will be set to the ADJUSTED_COST.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!enabled) return;
        if (!(event.getView().getPlayer() instanceof Player player)) return;
        if (shouldDisableLogic(player)) return;

        AnvilInventory anvil = event.getInventory();
        ItemStack result = event.getResult();
        if (result == null || result.getItemMeta() == null) return;

        // Update Anvil GUI to bypass "Too Expensive"
        anvil.setMaximumRepairCost(Integer.MAX_VALUE);
        int vanillaCost = anvil.getRepairCost();

        if (vanillaCost >= maxRepairCost) {
            anvil.setRepairCost(adjustedCost);
            event.getView().setProperty(InventoryView.Property.REPAIR_COST, adjustedCost);

            // Modify the item's NBT
            ItemMeta meta = result.getItemMeta();
            if (meta instanceof Repairable repairable) {
                repairable.setRepairCost(adjustedCost);
                result.setItemMeta(meta);
                event.setResult(result);
            }
        }
    }

    // If an item is already with a repair cost that is >= MAX_REPAIR_COST,
    // this will be set to the ADJUSTED_COST when clicking on the item again.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!enabled) return;
        if (event.getInventory().getType() != InventoryType.ANVIL) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (shouldDisableLogic(player)) return;

        AnvilInventory anvil = (AnvilInventory) event.getInventory();
        anvil.setMaximumRepairCost(Integer.MAX_VALUE);

        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Repairable repairable)) return;

        if (repairable.getRepairCost() >= maxRepairCost) {
            repairable.setRepairCost(adjustedCost);
            item.setItemMeta(meta);
            anvil.setItem(event.getSlot(), item);
        }
    }

    // Permission check.
    // If returns false, the logic will be applied to the player.
    private boolean shouldDisableLogic(Player player) {
        return !checkPermission && player.hasPermission("resetrepaircost.user");
    }
}
