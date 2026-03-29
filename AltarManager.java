package com.xai.ultimatelibrary;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class AltarManager implements Listener {

    private final UltimateLibraryPlugin plugin;
    private Location altarLocation;
    private boolean heirAlive = false;

    public AltarManager(UltimateLibraryPlugin plugin) {
        this.plugin = plugin;
    }

    public void setAltarLocation(Location loc) {
        this.altarLocation = loc;
    }

    public void setHeirAlive(boolean alive) {
        this.heirAlive = alive;
        updateSign();
    }

    private void updateSign() {
        if (altarLocation == null) return;

        Block signBlock = altarLocation.getBlock().getRelative(0, 1, 0);
        if (signBlock.getType() == Material.WALL_SIGN) {
            Sign sign = (Sign) signBlock.getState();

            if (heirAlive) {
                sign.setLine(0, "§c§lSacrifice");
                sign.setLine(1, "§c5 Hearts");
                sign.setLine(2, "§7for");
                sign.setLine(3, "§6Reaper’s Scythe");
            } else {
                sign.setLine(0, "§8§lSecret");
                sign.setLine(1, "§8Altar");
                sign.setLine(2, "");
                sign.setLine(3, "§7Heir must spawn");
            }
            sign.update(true);
        }
    }

    @EventHandler
    public void onAltarClick(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (altarLocation == null) return;

        Block clicked = event.getClickedBlock();
        if (clicked.getLocation().equals(altarLocation) && clicked.getType() == Material.RED_WOOL) {
            Player player = event.getPlayer();

            if (!heirAlive) {
                player.sendMessage("§cThe altar is still secret... The Heir must spawn first.");
                return;
            }

            // Sacrifice 5 permanent max health (5 hearts = 10 health points)
            double newMaxHealth = player.getMaxHealth() - 10;
            if (newMaxHealth < 2) {
                player.sendMessage("§cYou don't have enough hearts to sacrifice!");
                return;
            }

            player.setMaxHealth(newMaxHealth);
            player.setHealth(newMaxHealth);

            // Give Reaper’s Scythe (netherite base damage, max Sharpness 7)
            ItemStack scythe = new ItemStack(Material.DIAMOND_SWORD);
            scythe.addEnchantment(Enchantment.DAMAGE_ALL, 7);

            ItemMeta meta = scythe.getItemMeta();
            meta.setDisplayName("§6§lReaper’s Scythe");
            meta.setLore(java.util.Arrays.asList("§7Right-click to pull + web", "§7Lifesteal + Blindness on kill"));
            scythe.setItemMeta(meta);

            player.getInventory().addItem(scythe);

            player.sendMessage("§c§lYou sacrificed 5 hearts... The Reaper’s Scythe is now yours.");
        }
    }
}
