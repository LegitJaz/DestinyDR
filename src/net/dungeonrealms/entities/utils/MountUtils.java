package net.dungeonrealms.entities.utils;

import net.dungeonrealms.entities.types.Horse;
import net.dungeonrealms.enums.EnumEntityType;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Kieran on 9/18/2015.
 */
public class MountUtils {

    public static void spawnMount(UUID uuid, int rawSlot) {
        Player player = Bukkit.getPlayer(uuid);
        World world = ((CraftWorld) player.getWorld()).getHandle();
        switch (rawSlot) {
            //TODO: Add check for Achievements to see if Player has mount and can use it.
            case 2: {
                Horse mountHorse = new Horse(world, 0, 0.25D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0 , 0);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse;
                horse.setPassenger(player);
                HorseInventory horseInventory =  horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                horseInventory.setArmor(new ItemStack(Material.IRON_BARDING));
                player.playSound(player.getLocation(), Sound.HORSE_IDLE, 1F, 1F);
                player.sendMessage("Mount Spawned!");
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                break;
            }
            case 3: {
                Horse mountHorse = new Horse(world, 0, 0.3D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse;
                horse.setPassenger(player);
                HorseInventory horseInventory =  horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                horseInventory.setArmor(new ItemStack(Material.GOLD_BARDING));
                player.playSound(player.getLocation(), Sound.HORSE_IDLE, 1F, 1F);
                player.sendMessage("Mount Spawned!");
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                break;
            }
            case 4: {
                Horse mountHorse = new Horse(world, 0, 0.4D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse;
                horse.setPassenger(player);
                HorseInventory horseInventory =  horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                horseInventory.setArmor(new ItemStack(Material.DIAMOND_BARDING));
                player.playSound(player.getLocation(), Sound.HORSE_IDLE, 1F, 1F);
                player.sendMessage("Mount Spawned!");
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                break;
            }
            case 5: {
                Horse mountHorse = new Horse(world, 4, 0.4D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                mountHorse.getBukkitEntity().setPassenger(player);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse.getBukkitEntity();
                HorseInventory horseInventory = horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                player.playSound(player.getLocation(), Sound.HORSE_SKELETON_IDLE, 1F, 1F);
                player.sendMessage("Mount Spawned!");
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                break;
            }
            case 6: {
                Horse mountHorse = new Horse(world, 3, 0.4D, player.getUniqueId(), EnumEntityType.MOUNT);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(mountHorse, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mountHorse.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                org.bukkit.entity.Horse horse = (org.bukkit.entity.Horse) mountHorse;
                horse.setPassenger(player);
                HorseInventory horseInventory =  horse.getInventory();
                horseInventory.setSaddle(new ItemStack(Material.SADDLE));
                player.playSound(player.getLocation(), Sound.HORSE_ZOMBIE_IDLE, 1F, 1F);
                player.sendMessage("Mount Spawned!");
                EntityAPI.addPlayerMountList(player.getUniqueId(), mountHorse);
                player.closeInventory();
                break;
            }
        }
    }
}
