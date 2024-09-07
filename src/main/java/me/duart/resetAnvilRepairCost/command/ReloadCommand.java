package me.duart.resetAnvilRepairCost.command;

import me.duart.resetAnvilRepairCost.ResetAnvilRepairCost;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.annotation.Nonnull;
import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {
    private final ResetAnvilRepairCost anvilCost;

    public ReloadCommand(ResetAnvilRepairCost plugin) {
        this.anvilCost = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        String pluginName = ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + "AnvilCost" + ChatColor.DARK_GRAY + "] ";

        if (args.length == 0) {
            sender.sendMessage(pluginName + ChatColor.WHITE + anvilCost.getDescription().getVersion());
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("resetrepaircost.admin")) {
                sender.sendMessage(pluginName + ChatColor.RED + "You don't have permission to use this command!");
                return true;
            }
            anvilCost.onReload();
            sender.sendMessage(pluginName + ChatColor.GREEN + "Reloaded the plugin configuration!");
        } else {
            sender.sendMessage(pluginName + ChatColor.RED + "Invalid argument!");
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] strings) {
        if (commandSender.hasPermission("resetrepaircost.admin")) {
            return List.of("reload");
        }
        return List.of();
    }
}
