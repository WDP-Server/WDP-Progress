package com.wdp.progress.commands;

import com.wdp.progress.WDPProgressPlugin;
import com.wdp.progress.config.MessageManager;
import com.wdp.progress.data.PlayerData;
import com.wdp.progress.progress.ProgressCalculator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Main /progress command for players to view their progression
 * Supports: /progress, /progress <player>, /progress debug <player>
 */
public class ProgressCommand implements CommandExecutor, TabCompleter {
    
    private final WDPProgressPlugin plugin;
    private final MessageManager messages;
    private final DecimalFormat df;
    
    public ProgressCommand(WDPProgressPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
        int decimalPlaces = plugin.getConfigManager().getDecimalPlaces();
        StringBuilder pattern = new StringBuilder("#.");
        for (int i = 0; i < decimalPlaces; i++) {
            pattern.append("#");
        }
        this.df = new DecimalFormat(pattern.toString());
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Handle /progress debug <player>
        if (args.length >= 1 && args[0].equalsIgnoreCase("debug")) {
            return handleDebugCommand(sender, args);
        }
        
        // Check if viewing self or another player
        Player target;
        Player viewer;
        
        if (args.length == 0) {
            // View own progress
            if (!(sender instanceof Player)) {
                sender.sendMessage(messages.get("commands.progress.console-specify-player"));
                return true;
            }
            target = (Player) sender;
            viewer = (Player) sender;
        } else {
            // View another player's progress
            if (!(sender instanceof Player)) {
                sender.sendMessage(messages.get("commands.progress.console-no-gui"));
                return true;
            }
            
            viewer = (Player) sender;
            
            if (!sender.hasPermission("wdp.progress.view.others")) {
                sender.sendMessage(messages.get("commands.progress.no-permission-others"));
                return true;
            }
            
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(messages.get("commands.progress.player-not-found"));
                return true;
            }
        }
        
        // Get player data
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(messages.get("commands.progress.data-load-failed"));
            return true;
        }
        
                
        // Open the progress menu
        plugin.getProgressMenu().openProgressMenu(viewer, target);
        
        return true;
    }
    
    /**
     * Handle /progress debug <player> command
     */
    private boolean handleDebugCommand(CommandSender sender, String[] args) {
        // Permission check
        if (!sender.hasPermission("wdp.progress.debug")) {
            sender.sendMessage(messages.get("commands.debug.no-permission"));
            return true;
        }
        
        // Get target player
        Player target;
        
        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messages.get("commands.debug.usage"));
                return true;
            }
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(messages.get("commands.progress.player-not-found"));
                return true;
            }
        }
        
        // Get player data
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(messages.get("commands.progress.data-load-failed"));
            return true;
        }
        
        // Calculate current progress
        ProgressCalculator.ProgressResult result = plugin.getProgressCalculator().calculateProgress(target, data);
        
        // Display detailed debug information
        displayDebugInfo(sender, target, result, data);
        
        return true;
    }
    
    /**
     * Display detailed debug information
     */
    private void displayDebugInfo(CommandSender sender, Player target, 
                                  ProgressCalculator.ProgressResult result, PlayerData data) {
        
        sender.sendMessage("");
        sender.sendMessage(messages.get("commands.debug.header"));
        sender.sendMessage(messages.get("commands.debug.title", "player", target.getName()));
        sender.sendMessage(messages.get("commands.debug.header"));
        sender.sendMessage("");
        
        // Final score
        sender.sendMessage(messages.get("commands.debug.final-score", "score", df.format(result.getFinalScore())));
        sender.sendMessage("");
        
        // Category breakdown
        sender.sendMessage(messages.get("commands.debug.category-breakdown"));
        sender.sendMessage("");
        
        displayCategoryDebug(sender, "Advancements", result.getAdvancementsScore(), 
            plugin.getConfigManager().getCategoryWeight("advancements"));
        displayCategoryDebug(sender, "Experience", result.getExperienceScore(), 
            plugin.getConfigManager().getCategoryWeight("experience"));
        displayCategoryDebug(sender, "Equipment", result.getEquipmentScore(), 
            plugin.getConfigManager().getCategoryWeight("equipment"));
        displayCategoryDebug(sender, "Economy", result.getEconomyScore(), 
            plugin.getConfigManager().getCategoryWeight("economy"));
        displayCategoryDebug(sender, "Statistics", result.getStatisticsScore(), 
            plugin.getConfigManager().getCategoryWeight("statistics"));
        displayCategoryDebug(sender, "Achievements", result.getAchievementsScore(), 
            plugin.getConfigManager().getCategoryWeight("achievements"));
        
        sender.sendMessage("");
        
        // Death penalty
        double deathPenalty = result.getDeathPenalty();
        sender.sendMessage(messages.get("commands.debug.death-penalty", "penalty", df.format(deathPenalty)));
        sender.sendMessage("");
        
        // Calculation formula
        sender.sendMessage(messages.get("commands.debug.calculation-formula"));
        sender.sendMessage(messages.get("commands.debug.formula-text"));
        sender.sendMessage("");
        
        // Detailed stats
        sender.sendMessage(messages.get("commands.debug.player-statistics"));
        sender.sendMessage(messages.get("commands.debug.stat-level", "level", String.valueOf(target.getLevel())));
        sender.sendMessage(messages.get("commands.debug.stat-deaths", "deaths", String.valueOf(data.getTotalDeaths())));
        sender.sendMessage(messages.get("commands.debug.stat-achievements", "count", String.valueOf(data.getCompletedAchievements().size())));
        sender.sendMessage(messages.get("commands.debug.stat-last-updated", "time", 
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(data.getLastUpdated()))));
        
        if (plugin.getVaultIntegration() != null && plugin.getVaultIntegration().hasEconomy()) {
            double balance = plugin.getVaultIntegration().getBalance(target);
            sender.sendMessage(messages.get("commands.debug.stat-balance", "balance", df.format(balance)));
        }
        
        sender.sendMessage("");
        sender.sendMessage(messages.get("commands.debug.header"));
        sender.sendMessage("");
    }
    
    /**
     * Display individual category debug info
     */
    private void displayCategoryDebug(CommandSender sender, String name, double score, double weight) {
        double contribution = (score * weight) / 100.0;
        String bar = createSimpleBar(score);
        
        sender.sendMessage(ChatColor.GOLD + name + ":");
        sender.sendMessage(messages.get("commands.debug.category-score", "score", df.format(score), "bar", bar));
        sender.sendMessage(messages.get("commands.debug.category-weight", "weight", String.valueOf((int) weight)));
        sender.sendMessage(messages.get("commands.debug.category-contribution", "contribution", df.format(contribution)));
        sender.sendMessage("");
    }
    
    /**
     * Create a simple progress bar
     */
    private String createSimpleBar(double value) {
        int filled = (int) (value / 10); // 10 characters for 100
        int empty = 10 - filled;
        
        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.GREEN);
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        bar.append(ChatColor.DARK_GRAY);
        for (int i = 0; i < empty; i++) {
            bar.append("█");
        }
        
        return bar.toString();
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Suggest debug command
            if (sender.hasPermission("wdp.progress.debug") && "debug".startsWith(args[0].toLowerCase())) {
                completions.add("debug");
            }
            
            // Suggest online player names
            if (sender.hasPermission("wdp.progress.view.others")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            // Suggest player names for debug command
            if (sender.hasPermission("wdp.progress.debug")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}
