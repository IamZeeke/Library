package com.xai.ultimatelibrary;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class StructureBuilder {

    private final JavaPlugin plugin;

    public StructureBuilder(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void buildFullDungeon(Player player) {
        World world = player.getWorld();
        Location loc = player.getLocation();

        Vector direction = loc.getDirection().normalize().multiply(10);
        int cx = loc.getBlockX() + (int) direction.getX();
        int cz = loc.getBlockZ() + (int) direction.getZ();

        player.sendMessage("§6[Library] §eBuilding complete dungeon...");

        buildFloor1(world, cx, cz);
        buildFloor2(world, cx, cz);
        buildBasement(world, cx, cz);
        buildSacrificeAltar(world, cx, cz);

        // Auto spawn Mrs. Bali
        plugin.getBossManager().spawnMrsBaliInBasement(cx, cz);

        player.sendMessage("§a§lCOMPLETE DUNGEON BUILT! Mrs. Bali has spawned in the basement.");
    }

    public void regenerateDungeon(Player player) {
        buildFullDungeon(player);
    }

    // ====================== FLOOR 1 (y=70) ======================
    private void buildFloor1(World world, int cx, int cz) {
        int minX = cx - 20, maxX = cx + 20;
        int minZ = cz - 20, maxZ = cz + 20;
        int baseY = 70;

        clearArea(world, minX, maxX, minZ, maxZ, baseY - 5, baseY + 10);
        buildBasicStructure(world, minX, maxX, minZ, maxZ, baseY);
        buildStaircaseToFloor2(world, cx + 15, baseY, cz + 15);

        placeChestsAndLoot(world, cx, cz, baseY, 1);
        placeTrapsFloor1(world, cx, cz, baseY);
        placeSpawnersFloor1(world, cx, cz, baseY);
    }

    // ====================== FLOOR 2 (y=80) ======================
    private void buildFloor2(World world, int cx, int cz) {
        int minX = cx - 20, maxX = cx + 20;
        int minZ = cz - 20, maxZ = cz + 20;
        int baseY = 80;

        clearArea(world, minX, maxX, minZ, maxZ, 78, baseY + 10);
        buildBasicStructure(world, minX, maxX, minZ, maxZ, baseY);
        buildStaircaseToBasement(world, cx + 15, baseY, cz - 15);

        placeChestsAndLoot(world, cx, cz, baseY, 2);
        placeTrapsFloor2(world, cx, cz, baseY);
        placeSpawnersFloor2(world, cx, cz, baseY);
    }

    // ====================== BASEMENT (y=55) ======================
    private void buildBasement(World world, int cx, int cz) {
        int minX = cx - 20, maxX = cx + 20;
        int minZ = cz - 20, maxZ = cz + 20;
        int baseY = 55;

        clearArea(world, minX, maxX, minZ, maxZ, baseY - 10, baseY + 9);
        buildBasicStructure(world, minX, maxX, minZ, maxZ, baseY);

        // Boss arena red carpet
        for (int x = cx - 7; x <= cx + 7; x++) {
            for (int z = cz - 7; z <= cz + 7; z++) {
                world.getBlockAt(x, baseY + 1, z).setType(Material.RED_CARPET);
            }
        }

        placeChestsAndLoot(world, cx, cz, baseY, 3);
        placeTrapsBasement(world, cx, cz, baseY);
        placeSpawnersBasement(world, cx, cz, baseY);
    }

    private void clearArea(World world, int minX, int maxX, int minZ, int maxZ, int minY, int maxY) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    private void buildBasicStructure(World world, int minX, int maxX, int minZ, int maxZ, int baseY) {
        // Floor & Ceiling
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, baseY, z).setType(Material.COBBLESTONE);
                world.getBlockAt(x, baseY + 8, z).setType(Material.COBBLESTONE);
            }
        }

        // Outer walls
        for (int x = minX; x <= maxX; x++) {
            for (int y = baseY + 1; y <= baseY + 7; y++) {
                world.getBlockAt(x, y, minZ).setType(Material.COBBLESTONE);
                world.getBlockAt(x, y, maxZ).setType(Material.COBBLESTONE);
            }
        }
        for (int z = minZ; z <= maxZ; z++) {
            for (int y = baseY + 1; y <= baseY + 7; y++) {
                world.getBlockAt(minX, y, z).setType(Material.COBBLESTONE);
                world.getBlockAt(maxX, y, z).setType(Material.COBBLESTONE);
            }
        }

        // Bookshelves
        for (int x = minX + 2; x <= maxX - 2; x += 4) {
            for (int y = baseY + 1; y <= baseY + 6; y++) {
                world.getBlockAt(x, y, minZ + 1).setType(Material.BOOKSHELF);
                world.getBlockAt(x, y, maxZ - 1).setType(Material.BOOKSHELF);
            }
        }

        // Gray carpets
        for (int x = minX + 3; x <= maxX - 3; x++) {
            for (int z = minZ + 3; z <= maxZ - 3; z++) {
                Block b = world.getBlockAt(x, baseY + 1, z);
                b.setType(Material.CARPET);
                b.setData((byte) 7);
            }
        }

        // Chandeliers
        placeChandelier(world, (minX + maxX)/2 - 8, baseY + 7, (minZ + maxZ)/2 - 8);
        placeChandelier(world, (minX + maxX)/2 + 8, baseY + 7, (minZ + maxZ)/2 + 8);
    }

    private void placeChandelier(World world, int x, int y, int z) {
        world.getBlockAt(x, y, z).setType(Material.GLOWSTONE);
        world.getBlockAt(x, y - 1, z).setType(Material.OAK_FENCE);
        world.getBlockAt(x, y - 2, z).setType(Material.OAK_FENCE);
    }

    private void buildStaircaseToFloor2(World world, int x, int y, int z) {
        for (int i = 0; i < 5; i++) {
            world.getBlockAt(x + i, y + 1 + i, z).setType(Material.OAK_STAIRS);
        }
        for (int i = 0; i < 4; i++) {
            world.getBlockAt(x + 4, y + 1 + i, z).setType(Material.BOOKSHELF);
        }
    }

    private void buildStaircaseToBasement(World world, int x, int y, int z) {
        for (int i = 0; i < 5; i++) {
            world.getBlockAt(x + i, y - i, z).setType(Material.OAK_STAIRS);
        }
        for (int i = 0; i < 4; i++) {
            world.getBlockAt(x + 4, y - i, z).setType(Material.BOOKSHELF);
        }
    }

    private void placeChestsAndLoot(World world, int cx, int cz, int baseY, int floor) {
        // 12 Normal Loot Chests
        for (int i = 0; i < 12; i++) {
            int x = cx - 15 + (i % 6) * 5;
            int z = cz - 15 + (i / 6) * 8;
            Block b = world.getBlockAt(x, baseY + 2, z);
            b.setType(Material.CHEST);
            fillNormalLoot((Chest) b.getState(), floor);
        }

        // 10 Trash Chests
        for (int i = 0; i < 10; i++) {
            int x = cx - 12 + (i % 5) * 6;
            int z = cz + 10 + (i / 5) * 8;
            Block b = world.getBlockAt(x, baseY + 2, z);
            b.setType(Material.CHEST);
            fillTrashLoot((Chest) b.getState());
        }
    }

    private void fillNormalLoot(Chest chest, int floor) {
        var inv = chest.getInventory();
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        book.addEnchantment(Enchantment.DAMAGE_ALL, 3 + (int)(Math.random()*3));
        book.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2 + (int)(Math.random()*3));
        inv.addItem(book);

        inv.addItem(new ItemStack(Material.IRON_INGOT, 5 + (int)(Math.random()*10)));
        inv.addItem(new ItemStack(Material.COBWEB, 3));
        inv.addItem(new ItemStack(Material.OBSIDIAN, 2));
        inv.addItem(new ItemStack(Material.DIAMOND, 1 + (int)(Math.random()*2)));
        inv.addItem(new ItemStack(Material.GOLD_INGOT, 2 + (int)(Math.random()*4)));
        inv.addItem(new ItemStack(Material.POTION, 1));
        inv.addItem(new ItemStack(Material.STICK, 16));
        inv.addItem(new ItemStack(Material.ARROW, 12));
    }

    private void fillTrashLoot(Chest chest) {
        var inv = chest.getInventory();
        inv.addItem(new ItemStack(Material.RAW_FISH, 8 + (int)(Math.random()*12)));
        inv.addItem(new ItemStack(Material.COOKED_FISH, 6));
        inv.addItem(new ItemStack(Material.ROTTEN_FLESH, 12 + (int)(Math.random()*10)));
        inv.addItem(new ItemStack(Material.BONE, 8));
    }

    private void placeTrapsFloor1(World world, int cx, int cz, int baseY) {
        for (int i = 0; i < 4; i++) {
            int x = cx - 10 + i * 5;
            int z = cz - 10;
            world.getBlockAt(x, baseY + 2, z).setType(Material.GRAVEL);
            world.getBlockAt(x, baseY - 4, z).setType(Material.LAVA);
        }
    }

    private void placeTrapsFloor2(World world, int cx, int cz, int baseY) {
        placeTrapsFloor1(world, cx, cz, baseY);
    }

    private void placeTrapsBasement(World world, int cx, int cz, int baseY) {
        world.getBlockAt(cx + 5, baseY + 2, cz + 10).setType(Material.RED_WOOL);
        world.getBlockAt(cx + 5, baseY + 1, cz + 10).setType(Material.STONE_PLATE);
    }

    private void placeSpawnersFloor1(World world, int cx, int cz, int baseY) {
        createSpawner(world, cx - 8, baseY + 2, cz - 8, EntityType.ZOMBIE);
        createSpawner(world, cx + 8, baseY + 2, cz + 8, EntityType.SKELETON);
    }

    private void placeSpawnersFloor2(World world, int cx, int cz, int baseY) {
        createSpawner(world, cx - 8, baseY + 2, cz - 8, EntityType.VINDICATOR);
        createSpawner(world, cx + 8, baseY + 2, cz + 8, EntityType.ZOMBIE);
    }

    private void placeSpawnersBasement(World world, int cx, int cz, int baseY) {
        createSpawner(world, cx, baseY + 2, cz, EntityType.ZOMBIE);
    }

    private void createSpawner(World world, int x, int y, int z, EntityType type) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(Material.MOB_SPAWNER);
        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        spawner.setSpawnedType(type);
        spawner.setMinSpawnDelay(100);
        spawner.setMaxSpawnDelay(300);
        spawner.setSpawnCount(2);
        spawner.setMaxNearbyEntities(6);
        spawner.update(true);
    }

    private void buildSacrificeAltar(World world, int cx, int cz) {
        int ax = cx + 25;
        int ay = 55;
        int az = cz + 5;

        for (int x = ax - 4; x <= ax + 4; x++) {
            for (int z = az - 4; z <= az + 4; z++) {
                world.getBlockAt(x, ay, z).setType(Material.COBBLESTONE);
                world.getBlockAt(x, ay + 4, z).setType(Material.COBBLESTONE);
            }
        }

        world.getBlockAt(ax, ay + 1, az).setType(Material.OBSIDIAN);
        Block wool = world.getBlockAt(ax, ay + 2, az);
        wool.setType(Material.RED_WOOL);

        Block signBlock = world.getBlockAt(ax, ay + 3, az);
        signBlock.setType(Material.WALL_SIGN);
        Sign sign = (Sign) signBlock.getState();
        sign.setLine(0, "§8§lSecret");
        sign.setLine(1, "§8Altar");
        sign.setLine(2, "");
        sign.setLine(3, "§7Heir must spawn");
        sign.update(true);

        plugin.getAltarManager().setAltarLocation(wool.getLocation());
    }
}
