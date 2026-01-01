package com.wdp.progress.commands;

import com.wdp.progress.WDPProgressPlugin;
import com.wdp.progress.config.MessageManager;
import com.wdp.progress.data.PlayerData;
import com.wdp.progress.progress.ProgressCalculator;
import com.wdp.progress.ui.AdvancementAdminMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Admin command for progress management
 */
public class ProgressAdminCommand implements CommandExecutor, TabCompleter {
    
    private final WDPProgressPlugin plugin;
    private final MessageManager messages;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private final AdvancementAdminMenu advancementMenu;
    
    public ProgressAdminCommand(WDPProgressPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        this.advancementMenu = new AdvancementAdminMenu(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wdp.progress.admin")) {
            sender.sendMessage(messages.get("commands.admin.no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                return handleReload(sender);
                
            case "recalculate":
                return handleRecalculate(sender, args);
                
            case "set":
                return handleSet(sender, args);
                
            case "reset":
                return handleReset(sender, args);
                
            case "debug":
                return handleDebug(sender, args);
                
            case "advancements":
            case "adv":
                return handleAdvancements(sender, args);
                
            case "confirm-reset-adv":
                return handleConfirmResetAdvancements(sender, args);
                
            case "confirm-grant-adv":
                return handleConfirmGrantAdvancements(sender, args);
                
            default:
                sender.sendMessage(messages.get("commands.admin.unknown-subcommand"));
                return true;
        }
    }
    
    /**
     * Handle reload subcommand
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("wdp.progress.admin.reload")) {
            sender.sendMessage(messages.get("commands.admin.reload.no-permission"));
            return true;
        }
        
        sender.sendMessage(messages.get("commands.admin.reload.reloading"));
        
        if (plugin.reload()) {
            sender.sendMessage(messages.get("commands.admin.reload.success"));
        } else {
            sender.sendMessage(messages.get("commands.admin.reload.failed"));
        }
        
        return true;
    }
    
    /**
     * Handle recalculate subcommand
     */
    private boolean handleRecalculate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("wdp.progress.admin.recalculate")) {
            sender.sendMessage(messages.get("commands.admin.recalculate.no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(messages.get("commands.admin.recalculate.usage"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messages.get("commands.progress.player-not-found"));
            return true;
        }
        
        sender.sendMessage(messages.get("commands.admin.recalculate.calculating", "player", target.getName()));
        
        double newProgress = plugin.getPlayerDataManager().forceRecalculate(target.getUniqueId());
        
        sender.sendMessage(messages.get("commands.admin.recalculate.success", "value", df.format(newProgress)));
        
        return true;
    }
    
    /**
     * Handle set subcommand
     */
    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("wdp.progress.admin.set")) {
            sender.sendMessage(messages.get("commands.admin.set.no-permission"));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(messages.get("commands.admin.set.usage"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messages.get("commands.progress.player-not-found"));
            return true;
        }
        
        double value;
        try {
            value = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(messages.get("commands.admin.set.invalid-number", "value", args[2]));
            return true;
        }
        
        if (plugin.getPlayerDataManager().setProgress(target.getUniqueId(), value)) {
            sender.sendMessage(messages.get("commands.admin.set.success", "player", target.getName(), "value", df.format(value)));
        } else {
            sender.sendMessage(messages.get("commands.admin.set.failed"));
        }
        
        return true;
    }
    
    /**
     * Handle reset subcommand
     */
    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("wdp.progress.admin.reset")) {
            sender.sendMessage(messages.get("commands.admin.reset.no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(messages.get("commands.admin.reset.usage"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messages.get("commands.progress.player-not-found"));
            return true;
        }
        
        sender.sendMessage(messages.get("commands.admin.reset.confirm-warning", "player", target.getName()));
        sender.sendMessage(messages.get("commands.admin.reset.confirm-prompt"));
        
        // TODO: Implement confirmation system with a map and scheduler
        // For now, just reset immediately
        if (plugin.getPlayerDataManager().resetProgress(target.getUniqueId())) {
            sender.sendMessage(messages.get("commands.admin.reset.success", "player", target.getName()));
        } else {
            sender.sendMessage(messages.get("commands.admin.reset.failed"));
        }
        
        return true;
    }
    
    /**
     * Handle debug subcommand
     */
    private boolean handleDebug(CommandSender sender, String[] args) {
        if (!sender.hasPermission("wdp.progress.admin.debug")) {
            sender.sendMessage(messages.get("commands.admin.debug.no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(messages.get("commands.admin.debug.usage"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messages.get("commands.progress.player-not-found"));
            return true;
        }
        
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
        ProgressCalculator.ProgressResult result = plugin.getProgressCalculator().calculateProgress(target, data);
        
        sender.sendMessage("");
        sender.sendMessage(messages.get("commands.admin.debug.header"));
        sender.sendMessage(messages.get("commands.admin.debug.title", "player", target.getName()));
        sender.sendMessage(messages.get("commands.admin.debug.header"));
        sender.sendMessage("");
        sender.sendMessage(messages.get("commands.admin.debug.current-progress", "value", df.format(data.getCurrentProgress())));
        sender.sendMessage(messages.get("commands.admin.debug.last-progress", "value", df.format(data.getLastProgress())));
        sender.sendMessage(messages.get("commands.admin.debug.progress-delta", "value", df.format(data.getProgressDelta())));
        sender.sendMessage("");
        sender.sendMessage(messages.get("commands.admin.debug.category-scores"));
        sender.sendMessage(ChatColor.GRAY + "    Advancements: " + ChatColor.WHITE + df.format(result.getAdvancementsScore()));
        sender.sendMessage(ChatColor.GRAY + "    Experience: " + ChatColor.WHITE + df.format(result.getExperienceScore()));
        sender.sendMessage(ChatColor.GRAY + "    Equipment: " + ChatColor.WHITE + df.format(result.getEquipmentScore()));
        sender.sendMessage(ChatColor.GRAY + "    Economy: " + ChatColor.WHITE + df.format(result.getEconomyScore()));
        sender.sendMessage(ChatColor.GRAY + "    Statistics: " + ChatColor.WHITE + df.format(result.getStatisticsScore()));
        sender.sendMessage(ChatColor.GRAY + "    Achievements: " + ChatColor.WHITE + df.format(result.getAchievementsScore()));
        sender.sendMessage(ChatColor.GRAY + "    Death Penalty: " + ChatColor.RED + "-" + df.format(result.getDeathPenalty()));
        sender.sendMessage("");
        sender.sendMessage(messages.get("commands.admin.debug.category-weights"));
        sender.sendMessage(ChatColor.GRAY + "    Advancements: " + ChatColor.WHITE + plugin.getConfigManager().getCategoryWeight("advancements") + "%");
        sender.sendMessage(ChatColor.GRAY + "    Experience: " + ChatColor.WHITE + plugin.getConfigManager().getCategoryWeight("experience") + "%");
        sender.sendMessage(ChatColor.GRAY + "    Equipment: " + ChatColor.WHITE + plugin.getConfigManager().getCategoryWeight("equipment") + "%");
        sender.sendMessage(ChatColor.GRAY + "    Economy: " + ChatColor.WHITE + plugin.getConfigManager().getCategoryWeight("economy") + "%");
        sender.sendMessage(ChatColor.GRAY + "    Statistics: " + ChatColor.WHITE + plugin.getConfigManager().getCategoryWeight("statistics") + "%");
        sender.sendMessage(ChatColor.GRAY + "    Achievements: " + ChatColor.WHITE + plugin.getConfigManager().getCategoryWeight("achievements") + "%");
        sender.sendMessage("");
        sender.sendMessage(messages.get("commands.admin.debug.player-level", "level", String.valueOf(target.getLevel())));
        sender.sendMessage(messages.get("commands.admin.debug.completed-achievements", "count", String.valueOf(data.getCompletedAchievements().size())));
        String lastDeathText = data.getLastDeathTime() > 0 ? ((System.currentTimeMillis() - data.getLastDeathTime()) / 1000 + "s ago") : "Never";
        sender.sendMessage(messages.get("commands.admin.debug.last-death", "time", lastDeathText));
        sender.sendMessage("");
        
        if (plugin.getVaultIntegration() != null && plugin.getVaultIntegration().hasEconomy()) {
            sender.sendMessage(messages.get("commands.admin.debug.balance", "balance", df.format(plugin.getVaultIntegration().getBalance(target))));
        }
        
        sender.sendMessage("");
        sender.sendMessage(messages.get("commands.admin.debug.header"));
        sender.sendMessage("");
        
        return true;
    }
    
    /**
     * Handle advancements subcommand - opens the advancement admin menu
     */
    private boolean handleAdvancements(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("commands.admin.advancements.players-only"));
            return true;
        }
        
        Player admin = (Player) sender;
        
        if (!admin.isOp()) {
            sender.sendMessage(messages.get("commands.admin.advancements.op-only"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(messages.get("commands.admin.advancements.usage"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messages.get("commands.progress.player-not-found"));
            return true;
        }
        
        advancementMenu.openMenu(admin, target);
        return true;
    }
    
    /**
     * Confirm reset all advancements
     */
    private boolean handleConfirmResetAdvancements(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("commands.admin.advancements.players-only"));
            return true;
        }
        
        Player admin = (Player) sender;
        
        if (!admin.isOp()) {
            sender.sendMessage(messages.get("commands.admin.advancements.op-only"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(messages.get("commands.admin.advancements.confirm-reset-usage"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messages.get("commands.progress.player-not-found"));
            return true;
        }
        
        advancementMenu.resetAllAdvancements(target, admin);
        return true;
    }
    
    /**
     * Confirm grant all advancements
     */
    private boolean handleConfirmGrantAdvancements(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.get("commands.admin.advancements.players-only"));
            return true;
        }
        
        Player admin = (Player) sender;
        
        if (!admin.isOp()) {
            sender.sendMessage(messages.get("commands.admin.advancements.op-only"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(messages.get("commands.admin.advancements.confirm-grant-usage"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messages.get("commands.progress.player-not-found"));
            return true;
        }
        
        advancementMenu.grantAllAdvancements(target, admin);
        return true;
    }
    
    /**
     * Send help message
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(messages.get("commands.admin.help.header"));
        sender.sendMessage("");
        sender.sendMessage(messages.get("commands.admin.help.reload"));
        sender.sendMessage(messages.get("commands.admin.help.recalculate"));
        sender.sendMessage(messages.get("commands.admin.help.set"));
        sender.sendMessage(messages.get("commands.admin.help.reset"));
        sender.sendMessage(messages.get("commands.admin.help.debug"));
        sender.sendMessage(messages.get("commands.admin.help.advancements"));
        sender.sendMessage("");
        sender.sendMessage(messages.get("commands.admin.help.footer"));
        sender.sendMessage("");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Suggest subcommands
            List<String> subCommands = Arrays.asList("reload", "recalculate", "set", "reset", "debug", "advancements");
            for (String sub : subCommands) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            // Suggest player names
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            // Suggest common values
            completions.addAll(Arrays.asList("1", "25", "50", "75", "100"));
        }
        
        return completions;
    }
}
