package com.ryan.playertracker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class GUIHandler implements Listener {
    
    private static final String guiName = "Who would you like to track?";
    
    
    public static void openGUI(Player player) {
        int playerAmount = player.getWorld().getPlayers().size();
        int guiSize;
        
        if (playerAmount <= 9) {
            guiSize = 9;
        } else if (playerAmount <= 18) {
            guiSize = 18;
        } else if (playerAmount <= 27) {
            guiSize = 27;
        } else if (playerAmount <= 36) {
            guiSize = 36;
        } else if (playerAmount <= 45) {
            guiSize = 45;
        } else if (playerAmount <= 54) {
            guiSize = 54;
        } else {
            player.sendMessage("Too many players!");
            return;
        }
        
        Inventory gui = Bukkit.createInventory(player, guiSize, guiName);
        
        for (int i = 0; i < playerAmount; i++) {
            Player playerI = player.getWorld().getPlayers().get(i);
            
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            headMeta.setDisplayName(playerI.getName());
            headMeta.setOwningPlayer(playerI);
            head.setItemMeta(headMeta);
            gui.addItem(head);
        }
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public static void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase(guiName) && event.getCurrentItem() != null) {
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            player.closeInventory();
            if (event.getClickedInventory().getType() == InventoryType.CHEST) {
    
                try {
                    Player clickedPlayer = Bukkit.getPlayerExact(event.getCurrentItem().getItemMeta().getDisplayName());
                    Main.getPlugin().giveCompass(player, clickedPlayer);
                } catch (NullPointerException exception) {
                    player.sendMessage(ChatColor.RED + "That player could not be found");
                }
            }
        }
    }
}