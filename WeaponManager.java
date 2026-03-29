package com.xai.ultimatelibrary;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class WeaponManager implements Listener {

    private final UltimateLibraryPlugin plugin;
    private final HashMap<UUID, Long> glitchCooldown = new HashMap<>();

    public WeaponManager(UltimateLibraryPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWeaponUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.DIAMOND_SWORD) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) return;

        String name = meta.getDisplayName();

        // Reaper’s Scythe Right-Click
        if (name.contains("Reaper’s Scythe") && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            activateReaperScythe(player);
        }

        // Glitch Shard Right-Click
        if (name.contains("Glitch Shard") && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            activateGlitchShard(player);
        }
    }

    private void activateReaperScythe(Player player) {
        // Suck nearby players in + auto web them
        for (Player p : player.getWorld().getPlayers()) {
            if (p != player && p.getLocation().distance(player.getLocation()) < 8) {
                Vector direction = player.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.2);
                p.setVelocity(direction);
                p.getLocation().getBlock().setType(Material.COBWEB);
            }
        }

        player.sendMessage("§6§lReaper’s Scythe §7activated - Pull & Web!");
    }

    private void activateGlitchShard(Player player) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // 1 minute 15 seconds cooldown (75 seconds = 75000 ms)
        if (glitchCooldown.containsKey(uuid) && currentTime - glitchCooldown.get(uuid) < 75000) {
            player.sendMessage("§cGlitch Shard is on cooldown! (" + ((75000 - (currentTime - glitchCooldown.get(uuid))) / 1000) + "s left)");
            return;
        }

        glitchCooldown.put(uuid, currentTime);

        // Glitch nearby players
        for (Player p : player.getWorld().getPlayers()) {
            if (p != player && p.getLocation().distance(player.getLocation()) < 10) {
                // Nausea for 10 seconds
                p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));

                // Temporarily lock 2 hearts for 15 seconds
                double originalMax = p.getMaxHealth();
                double lockedHealth = Math.max(2, originalMax - 4); // lock last 2 hearts

                p.setMaxHealth(lockedHealth);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (p.isOnline()) {
                            p.setMaxHealth(originalMax);
                            p.sendMessage("§aYour locked hearts have recovered.");
                        }
                    }
                }.runTaskLater(plugin, 300L); // 15 seconds
            }
        }

        player.sendMessage("§5§lGlitch Shard §7activated! Targets glitched for 15 seconds.");
    }

    // Lifesteal + Blindness on kill for Reaper’s Scythe
    @EventHandler
    public void onReaperScytheKill(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();
        ItemStack item = attacker.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.DIAMOND_SWORD) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) return;

        if (meta.getDisplayName().contains("Reaper’s Scythe")) {
            if (event.getEntity() instanceof Player) {
                Player victim = (Player) event.getEntity();
                if (event.getFinalDamage() >= victim.getHealth()) {
                    // Lifesteal
                    double healAmount = Math.min(4.0, attacker.getMaxHealth() - attacker.getHealth());
                    attacker.setHealth(attacker.getHealth() + healAmount);

                    // Blindness to nearby players for 15 seconds
                    for (Player p : attacker.getWorld().getPlayers()) {
                        if (p.getLocation().distance(attacker.getLocation()) < 12) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300, 0));
                        }
                    }

                    // 50% chance for extra hearts (0.5 or 1)
                    if (Math.random() < 0.5) {
                        double extra = Math.random() < 0.5 ? 1.0 : 2.0;
                        attacker.setMaxHealth(attacker.getMaxHealth() + extra);
                        attacker.sendMessage("§6§lReaper’s Scythe §7granted you extra hearts!");
                    }
                }
            }
        }
    }
}
