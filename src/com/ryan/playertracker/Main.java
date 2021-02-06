package com.ryan.playertracker;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    
    private static Main plugin;
    private final String syntaxError = ChatColor.RED + "Syntax: /track or /track <player>";
    
    public static Main getPlugin() {
        return plugin;
    }
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new GUIHandler(), this);
        plugin = this;
    }
    
    @Override
    public void onDisable() {
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players!");
            return true;
        }
        
        // makes sure it doesn't crash if there are no args
        if (args.length == 0) {
            Player player = (Player) sender;
            GUIHandler.openGUI(player);
            return true;
        }
        
        if (label.equalsIgnoreCase("track")) {
            Player player = (Player) sender;
            try {
                giveCompass(player, Bukkit.getPlayerExact(args[0]));
            } catch (NullPointerException exception) {
                player.sendMessage(ChatColor.RED + "Could not find that player.");
            }
            
        } else if (label.equalsIgnoreCase("help")) {
            sender.sendMessage(syntaxError);
        } else {
            sender.sendMessage(syntaxError);
        }
        
        return true;
    }
    
    // updates the compass when it is right clicked
    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            
            if (event.hasItem() && event.getMaterial() == Material.COMPASS && event.getItem().getItemMeta().getDisplayName().contains("Tracker")) {
                CompassMeta compassMeta = (CompassMeta) event.getItem().getItemMeta();
                PersistentDataContainer data = compassMeta.getPersistentDataContainer();
                Player player = event.getPlayer();
                
                if (data.has(new NamespacedKey(this, "player"), PersistentDataType.STRING)) {
                    Player tracked = Bukkit.getPlayerExact(data.get(new NamespacedKey(this, "player"), PersistentDataType.STRING));
                    
                    if (tracked != null && tracked.isOnline()) {
                        compassMeta.setLodestone(tracked.getLocation());
                        event.getItem().setItemMeta(compassMeta);
                        player.sendMessage(ChatColor.GREEN + "Updated location of " + tracked.getDisplayName());
                    } else if (tracked != null && !tracked.isOnline()) {
                        player.sendMessage(ChatColor.RED + "That player is offline!.");
                    } else {
                        player.sendMessage(ChatColor.RED + "That player couldn't be found!");
                    }
                    
                } else {
                    player.sendMessage(ChatColor.RED + "For whatever reason we can't find who to track. Please run /track again.");
                }
            }
        }
    }
    
    // gets a compass with data to keep track of the player to be tracked
    private ItemStack getCompass(Player tracked) {
        
        ItemStack compassItem = new ItemStack(Material.COMPASS);
        CompassMeta compassMeta = (CompassMeta) compassItem.getItemMeta();
        
        // TODO: switch to using uuids?
        PersistentDataContainer data = compassMeta.getPersistentDataContainer();
        data.set(new NamespacedKey(this, "player"), PersistentDataType.STRING, tracked.getName());
        
        compassMeta.setLodestoneTracked(false);
        compassMeta.setLodestone(tracked.getLocation());
        compassMeta.setDisplayName(tracked.getDisplayName() + " Tracker");
        compassItem.setItemMeta(compassMeta);
        
        return compassItem;
    }
    
    // gives the compass to the player
    public void giveCompass(Player tracker, Player tracked) {
        System.out.println("[Player Tracker] " + tracker.getDisplayName() + " is now tracking " + tracked.getDisplayName());
        
        if (tracker.getInventory().firstEmpty() == -1) {
            Location location = tracker.getLocation();
            World world = tracker.getWorld();
            
            world.dropItemNaturally(location, getCompass(tracked));
            
        } else {
            tracker.getInventory().addItem(getCompass(tracked));
        }
        
        tracker.sendMessage(ChatColor.GREEN + "Now tracking " + tracked.getDisplayName());
    }
}