package com.xai.ultimatelibrary;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BossManager implements Listener {

    private final UltimateLibraryPlugin plugin;
    private Zombie mrsBali;
    private Zombie heir;
    private boolean heirAlive = false;
    private int heirKills = 0;
    private double currentHeirMaxHealth = 100.0;

    public BossManager(UltimateLibraryPlugin plugin) {
        this.plugin = plugin;
    }

    // Called automatically after /buildlibrary
    public void spawnMrsBaliInBasement(int cx, int cz) {
        Location spawnLoc = new Location(plugin.getServer().getWorlds().get(0), cx, 57, cz);

        mrsBali = (Zombie) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);

        mrsBali.setCustomName("§c§lMrs. Bali");
        mrsBali.setCustomNameVisible(true);
        mrsBali.setMaxHealth(200);
        mrsBali.setHealth(200);
        mrsBali.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.32);
        mrsBali.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(14);

        // Protection 2 Iron Armor (as requested)
        ItemStack helmet = new ItemStack(Material.IRON_HELMET);
        helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
        chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
        leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        ItemStack boots = new ItemStack(Material.IRON_BOOTS);
        boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);

        mrsBali.getEquipment().setHelmet(helmet);
        mrsBali.getEquipment().setChestplate(chestplate);
        mrsBali.getEquipment().setLeggings(leggings);
        mrsBali.getEquipment().setBoots(boots);
        mrsBali.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD));

        startMrsBaliAI();
    }

    private void startMrsBaliAI() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (mrsBali == null || mrsBali.isDead()) {
                    cancel();
                    return;
                }

                // Fire projectiles
                if (Math.random() < 0.45) {
                    mrsBali.getWorld().spawnEntity(mrsBali.getEyeLocation().add(0, 1, 0), EntityType.SMALL_FIREBALL);
                }

                // Devour pull
                for (Player p : mrsBali.getWorld().getPlayers()) {
                    if (p.getLocation().distance(mrsBali.getLocation()) < 3.5) {
                        p.damage(8);
                        Vector pull = mrsBali.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(1.1);
                        p.setVelocity(pull);
                    }
                }
            }
        }.runTaskTimer(plugin, 15L, 25L);
    }

    @EventHandler
    public void onMrsBaliHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() == mrsBali && event.getEntity() instanceof Player) {
            if (Math.random() < 0.28) {
                reduceArmorDurability((Player) event.getEntity(), 0.12);
            }
        }
    }

    private void reduceArmorDurability(Player p, double percent) {
        for (ItemStack armor : p.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR) {
                if (Math.random() < 0.12) {
                    short newDur = (short) (armor.getDurability() + (armor.getType().getMaxDurability() * percent));
                    armor.setDurability(newDur);
                }
            }
        }
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (event.getEntity() == mrsBali) {
            Location loc = mrsBali.getLocation();
            dropGlitchShard(loc);   // Glitch Shard from Mrs. Bali
            spawnHeir(loc);
        }
    }

    private void dropGlitchShard(Location loc) {
        ItemStack shard = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = shard.getItemMeta();
        meta.setDisplayName("§5§lGlitch Shard");
        meta.setLore(java.util.Arrays.asList("§7Starts at Sharpness 1", "§7Gains +1 Sharpness per kill (max 7)"));
        shard.setItemMeta(meta);
        shard.addEnchantment(Enchantment.DAMAGE_ALL, 1); // Starts at Sharpness 1
        loc.getWorld().dropItemNaturally(loc, shard);
    }

    private void spawnHeir(Location loc) {
        heir = (Zombie) loc.getWorld().spawnEntity(loc.add(0, 1, 0), EntityType.ZOMBIE);

        heir.setCustomName("§8§l?");
        heir.setCustomNameVisible(true);
        currentHeirMaxHealth = 100.0;
        heir.setMaxHealth(currentHeirMaxHealth);
        heir.setHealth(currentHeirMaxHealth);

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 8);
        sword.addEnchantment(Enchantment.SWEEPING_EDGE, 2);
        heir.getEquipment().setItemInHand(sword);

        heirAlive = true;
        plugin.getAltarManager().setHeirAlive(true);

        startHeirAI();
    }

    private void startHeirAI() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (heir == null || heir.isDead()) {
                    cancel();
                    heirAlive = false;
                    plugin.getAltarManager().setHeirAlive(false);
                    return;
                }

                Player target = getStrongestPlayer();

                // Attack any player on sight, prioritize strongest
                for (Player p : heir.getWorld().getPlayers()) {
                    if (p.getLocation().distance(heir.getLocation()) < 14) {
                        heir.setTarget(p);
                        break;
                    }
                }
                if (target != null) {
                    heir.setTarget(target);
                }

                // Fire projectiles
                if (Math.random() < 0.40) {
                    heir.getWorld().spawnEntity(heir.getEyeLocation().add(0, 1, 0), EntityType.SMALL_FIREBALL);
                }

                // Web attack
                if (Math.random() < 0.35) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            heir.getLocation().add(dx, 1, dz).getBlock().setType(Material.COBWEB);
                        }
                    }
                }

                // Web + TNT
                if (Math.random() < 0.18) {
                    Location t = target != null ? target.getLocation() : heir.getLocation();
                    t.add(0, 1, 0).getBlock().setType(Material.COBWEB);
                    t.add(0, 1, 0).getBlock().setType(Material.TNT);
                }

                // Bow attack
                if (Math.random() < 0.28) {
                    heir.getWorld().spawnEntity(heir.getEyeLocation(), EntityType.ARROW)
                            .setVelocity((target != null ? target.getLocation() : heir.getLocation()).toVector()
                                    .subtract(heir.getLocation().toVector()).normalize().multiply(1.9));
                }

                // TNT minecart
                if (Math.random() < 0.15) {
                    Location mineLoc = heir.getLocation().add(0, 1, 0);
                    org.bukkit.entity.minecart.ExplosiveMinecart minecart = 
                        (org.bukkit.entity.minecart.ExplosiveMinecart) heir.getWorld().spawnEntity(mineLoc, EntityType.MINECART_TNT);
                    if (target != null) {
                        minecart.setVelocity(target.getLocation().toVector().subtract(heir.getLocation().toVector()).normalize().multiply(0.9));
                    }
                }

                // Summon Swarm
                if (Math.random() < 0.22) {
                    for (int i = 0; i < 3; i++) {
                        Location sLoc = heir.getLocation().add(Math.random() * 4 - 2, 1, Math.random() * 4 - 2);
                        Zombie minion = (Zombie) heir.getWorld().spawnEntity(sLoc, EntityType.ZOMBIE);
                        minion.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
                    }
                }
            }
        }.runTaskTimer(plugin, 25L, 45L);
    }

    private Player getStrongestPlayer() {
        Player strongest = null;
        double bestScore = -1;
        for (Player p : heir.getWorld().getPlayers()) {
            double score = p.getHealth() + (p.getInventory().getArmorContents().length * 5);
            if (score > bestScore) {
                strongest = p;
                bestScore = score;
            }
        }
        return strongest;
    }

    private void dropRulersDemise(Location loc) {
        ItemStack demise = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = demise.getItemMeta();
        meta.setDisplayName("§4§lRuler’s Demise");
        meta.setLore(java.util.Arrays.asList("§7When holder dies:", "§7Deals 150 damage in 5×5 radius"));
        demise.setItemMeta(meta);
        loc.getWorld().dropItemNaturally(loc, demise);
    }

    @EventHandler
    public void onHeirDeath(EntityDeathEvent event) {
        if (event.getEntity() == heir) {
            dropRulersDemise(heir.getLocation());
            heirAlive = false;
            plugin.getAltarManager().setHeirAlive(false);
        }
    }

    // Final Beckoning at 5 health
    @EventHandler
    public void onHeirLowHealth(EntityDamageByEntityEvent event) {
        if (event.getEntity() == heir) {
            double remaining = heir.getHealth() - event.getFinalDamage();
            if (remaining <= 5 && remaining > 0) {
                for (Player p : heir.getWorld().getPlayers()) {
                    if (p.getLocation().distance(heir.getLocation()) < 6.5) {
                        p.damage(150);
                    }
                }
                heir.setHealth(0);
            }
        }
    }

    // Scaling: +30 max health per kill
    @EventHandler
    public void onHeirKill(EntityDeathEvent event) {
        if (heir != null && !heir.isDead() && event.getEntity() != heir) {
            heirKills++;
            currentHeirMaxHealth += 30;
            heir.setMaxHealth(currentHeirMaxHealth);
            heir.setHealth(currentHeirMaxHealth);

            double newSpeed = 0.23 + (heirKills * 0.012);
            heir.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(newSpeed);
            heir.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(10 + (heirKills * 1.2));
        }
    }
}
