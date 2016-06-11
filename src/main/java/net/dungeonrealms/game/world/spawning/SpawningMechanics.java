package net.dungeonrealms.game.world.spawning;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.*;
import net.dungeonrealms.game.world.entities.types.monsters.EntityGolem;
import net.dungeonrealms.game.world.entities.types.monsters.base.*;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by Chase on Sep 28, 2015
 */
public class SpawningMechanics implements GenericMechanic {

    private static ArrayList<BaseMobSpawner> ALLSPAWNERS = new ArrayList<>();
    private static ArrayList<EliteMobSpawner> ELITESPAWNERS = new ArrayList<>();
    public static ArrayList<String> SPAWNER_CONFIG = new ArrayList<>();
    public static ArrayList<BaseMobSpawner> BanditTroveSpawns = new ArrayList<>();
    private static SpawningMechanics instance;


    private static void initAllSpawners() {
        ALLSPAWNERS.forEach(BaseMobSpawner::init);
        ELITESPAWNERS.forEach(EliteMobSpawner::init);
    }

    private static void killAll() {
        ALLSPAWNERS.stream().forEach(mobSpawner -> {
            mobSpawner.kill();
            mobSpawner.getArmorstand().getBukkitEntity().remove();
            mobSpawner.getArmorstand().getWorld().removeEntity(mobSpawner.getArmorstand());
        });
        ELITESPAWNERS.stream().forEach(eliteMobSpawner -> {
            eliteMobSpawner.kill();
            eliteMobSpawner.getArmorstand().getBukkitEntity().remove();
            eliteMobSpawner.getArmorstand().getWorld().removeEntity(eliteMobSpawner.getArmorstand());
        });
    }

    private static void loadBaseSpawners() {
    	Utils.log.info("LOADING ALL DUNGEON REALMS MONSTERS...");
        SPAWNER_CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("spawners");
        for (String line : SPAWNER_CONFIG) {
            if (line == null || line.equalsIgnoreCase("null")) {
                continue;
            }
            boolean isElite = false;
            if (line.contains("*")) {
                isElite = true;
            }
            String[] coords = line.split("=")[0].split(",");
            double x, y, z;
            x = Double.parseDouble(coords[0]);
            y = Double.parseDouble(coords[1]);
            z = Double.parseDouble(coords[2]);
            String tierString = line.substring(line.indexOf(":"), line.indexOf(";"));
            tierString = tierString.substring(1);
            int tier = Integer.parseInt(tierString);
            Character strAmount = line.charAt(line.indexOf(";") + 1);
            int spawnAmount = Integer.parseInt(String.valueOf(strAmount));
            String monster = line.split("=")[1].split(":")[0];
            String spawnRange = String.valueOf(line.charAt(line.lastIndexOf("@") - 1));
            int spawnDelay = Integer.parseInt(line.substring(line.lastIndexOf("@") + 1, line.indexOf("#")));
            if (spawnDelay < 20) {
                if (!isElite) {
                    spawnDelay = 20;
                } else {
                    spawnDelay = 60;
                }
            }
            String locationRange[] = line.substring(line.indexOf("#") + 1, line.lastIndexOf("$")).split("-");
            int minXZ = Integer.parseInt(locationRange[0]);
            int maxXZ = Integer.parseInt(locationRange[1]);
            if (!isElite) {
                BaseMobSpawner spawner;
                if (spawnRange.equalsIgnoreCase("+")) {
                    spawner = new BaseMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "high", spawnDelay, minXZ, maxXZ);
                } else {
                    spawner = new BaseMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "low", spawnDelay, minXZ, maxXZ);
                }
                ALLSPAWNERS.add(spawner);
            } else {
                //TODO: Dangerous code!!! REMOVE BEFORE RELEASE!!!
                spawnDelay = 60;
                EliteMobSpawner spawner;
                if (spawnRange.equalsIgnoreCase("+")) {
                    spawner = new EliteMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, ELITESPAWNERS.size(), "high", spawnDelay, minXZ, maxXZ);
                } else {
                    spawner = new EliteMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, ELITESPAWNERS.size(), "low", spawnDelay, minXZ, maxXZ);
                }
                ELITESPAWNERS.add(spawner);
            }
        }
        //TODO: Dungeons.
        /*ArrayList<String> BANDIT_CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("banditTrove");
        Utils.log.info("LOADING DUNGEON SPAWNS...");
        for(String line : BANDIT_CONFIG){
            if (line == null || line.equalsIgnoreCase("null"))
                continue;
            String[] coords = line.split("=")[0].split(",");
            double x, y, z;
            x = Double.parseDouble(coords[0]);
            y = Double.parseDouble(coords[1]);
            z = Double.parseDouble(coords[2]);
            String tierString = line.substring(line.indexOf(":"), line.indexOf(";"));
            tierString = tierString.substring(1);
            int tier = Integer.parseInt(tierString);
            String stringAmount = line.split(";")[1].replace("-", "");
            stringAmount = stringAmount.replace("+", "");
            int spawnAmount = Integer.parseInt(stringAmount);
            String monster = line.split("=")[1].split(":")[0];
            int spawnDelay = 0;
            BaseMobSpawner spawner;
            spawner = new BaseMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, BanditTroveSpawns.size(), "high", spawnDelay, 1, 2);
            spawner.setDungeonSpawner(true);
            BanditTroveSpawns.add(spawner);
        }
        Utils.log.info("FINISHED LOADING DUNGEON SPAWNS");*/
        SpawningMechanics.initAllSpawners();
        Bukkit.getWorlds().get(0).getEntities().forEach(entity -> {
            ((CraftEntity) entity).getHandle().damageEntity(DamageSource.GENERIC, 20f);
            entity.remove();
        });
        Bukkit.getWorlds().get(0).getLivingEntities().forEach(entity -> ((CraftEntity) entity).getHandle().damageEntity(DamageSource.GENERIC, 20f));
    }

    public static void loadSpawner(String line) {
        String[] coords = line.split("=")[0].split(",");
        double x, y, z;
        x = Double.parseDouble(coords[0]);
        y = Double.parseDouble(coords[1]);
        z = Double.parseDouble(coords[2]);
        String tierString = line.substring(line.indexOf(":"), line.indexOf(";"));
        tierString = tierString.substring(1);
        int tier = Integer.parseInt(tierString);
        Character strAmount = line.charAt(line.indexOf(";") + 1);
        int spawnAmount = Integer.parseInt(String.valueOf(strAmount));
        String monster = line.split("=")[1].split(":")[0];
        String spawnRange = String.valueOf(line.charAt(line.lastIndexOf("@") - 1));
        BaseMobSpawner spawner;
        int spawnDelay = Integer.parseInt(line.substring(line.lastIndexOf("@") + 1, line.indexOf("#")));
        if (spawnDelay < 20) {
            spawnDelay = 20;
        }
        String locationRange[] = line.substring(line.indexOf("#") + 1, line.indexOf("$")).split("-");
        int minXZ = Integer.parseInt(locationRange[0]);
        int maxXZ = Integer.parseInt(locationRange[1]);
        if (spawnRange.equalsIgnoreCase("+")) {
            spawner = new BaseMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "high", spawnDelay, minXZ, maxXZ);
        } else {
            spawner = new BaseMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "low", spawnDelay, minXZ, maxXZ);
        }
        ALLSPAWNERS.add(spawner);
        spawner.init();
    }
    
    public static void remove(BaseMobSpawner mobSpawner) {
        ALLSPAWNERS.remove(mobSpawner);
    }

    /**
     * @param monsEnum
     * @return
     */
    public static Entity getMob(World world, int tier, EnumMonster monsEnum) {
        EnumEntityType type = EnumEntityType.HOSTILE_MOB;
        Entity entity;
        switch (monsEnum) {
            case Bandit:
            case Bandit1:
                entity = new EntityBandit(world, tier, type, monsEnum);
                break;
            case RangedPirate:
                entity = new EntityRangedPirate(world, type, tier);
                break;
            case Pirate:
                entity = new EntityPirate(world, EnumMonster.Pirate, tier);
                break;
            case MayelPirate:
                entity = new EntityPirate(world, EnumMonster.MayelPirate, tier);
                break;
            case FireImp:
                entity = new EntityFireImp(world, tier, type);
                break;
            case Troll1:
            case Troll:
                entity = new BasicMeleeMonster(world, EnumMonster.Troll, tier);
                break;
            case Goblin:
                entity = new BasicMeleeMonster(world, EnumMonster.Goblin, tier);
                break;
            case Mage:
                entity = new BasicMageMonster(world, EnumMonster.Mage, tier);
                break;
            case Spider:
            case Spider1:
            case Spider2:
                entity = new DRSpider(world, EnumMonster.Spider, tier);
                break;
            case Golem:
                entity = new EntityGolem(world, tier, type);
                break;
            case Naga:
            	if (new Random().nextBoolean()) {
                    entity = new BasicMageMonster(world, EnumMonster.Naga, tier);
                } else {
                    entity = new BasicMeleeMonster(world, EnumMonster.Naga, tier);
                }
                break;
            case Tripoli1:
            case Tripoli:
                entity = new BasicMeleeMonster(world, EnumMonster.Tripoli, tier);
                break;
            case Blaze:
                entity = new BasicEntityBlaze(world, EnumMonster.Blaze, tier);
                break;
            case Skeleton2:
            case Skeleton1:
            case Skeleton:
                entity = new BasicEntitySkeleton(world, tier, monsEnum);
                break;
            case FrozenSkeleton:
                entity = new DRWitherSkeleton(world, monsEnum, tier);
                break;
            case Wither:
                entity = new DRWitherSkeleton(world, monsEnum, tier);
                break;
            case MagmaCube:
                entity = new DRMagma(world, EnumMonster.MagmaCube, tier);
                break;
            case Daemon:
                entity = new DRPigman(world, EnumMonster.Daemon, tier);
                break;
            case Daemon2:
                entity = new BasicMageMonster(world, EnumMonster.Daemon2, tier);
                break;
            case Silverfish:
                entity = new DRSilverfish(world, EnumMonster.Silverfish, tier);
                break;
            case SpawnOfInferno:
                entity = new DRMagma(world, EnumMonster.SpawnOfInferno, tier);
                ((DRMagma) entity).setSize(4);
                break;
            case GreaterAbyssalDemon:
                entity = new DRSilverfish(world, EnumMonster.GreaterAbyssalDemon, tier);
                break;
            case Monk:
            	entity = new BasicMeleeMonster(world, EnumMonster.Monk, tier);
            	break;
            case Lizardman:
            	entity = new BasicMeleeMonster(world, EnumMonster.Lizardman, tier);
            	break;
            case Zombie:
            	entity = new BasicMeleeMonster(world, EnumMonster.Zombie, tier);
            	break;
            case Wolf:
                entity = new DRWolf(world, EnumMonster.Wolf, tier);
                break;
            case Undead:
                entity = new BasicMeleeMonster(world, EnumMonster.Undead, tier);
                break;
            case Witch:
                entity = new DRWitch(world, EnumMonster.Witch, tier);
                break;
            case Pig:
                entity = new EntityPig(world);
                break;
            case Bat:
                entity = new EntityBat(world);
                break;
            case Cow:
                entity = new EntityCow(world);
                break;
            default:
                Utils.log.info("[SPAWNING] Tried to create " + monsEnum.idName + " but it has failed.");
                return null;
        }
        return entity;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        loadBaseSpawners();
    }

    @Override
    public void stopInvocation() {
        killAll();
        Bukkit.getWorlds().get(0).getEntities().forEach(entity -> {
           ((CraftWorld)entity.getWorld()).getHandle().removeEntity(((CraftEntity) entity).getHandle());
            entity.remove();
        });
        Bukkit.getWorlds().get(0).getLivingEntities().forEach(entity -> {
            ((CraftWorld)entity.getWorld()).getHandle().removeEntity(((CraftEntity) entity).getHandle());
            entity.remove();
        });
    }

    /**
     * @return
     */
    public static SpawningMechanics getInstance() {
        if (instance == null) {
            instance = new SpawningMechanics();
        }
        return instance;
    }

    public List<BaseMobSpawner> getChunkMobBaseSpawners(Chunk chunk) {
        return ALLSPAWNERS.stream().filter(mobSpawner -> mobSpawner.getLoc().getChunk().equals(chunk)).collect(Collectors.toList());
    }

    public List<EliteMobSpawner> getChunkEliteMobSpawners(Chunk chunk) {
        return ELITESPAWNERS.stream().filter(mobSpawner -> mobSpawner.getLocation().getChunk().equals(chunk)).collect(Collectors.toList());
    }


    public static ArrayList<BaseMobSpawner> getALLSPAWNERS() {
        return ALLSPAWNERS;
    }

    public static ArrayList<EliteMobSpawner> getELITESPAWNERS() {
        return ELITESPAWNERS;
    }
}