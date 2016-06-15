package net.dungeonrealms.game.world.entities.types.monsters.boss;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.boss.subboss.InfernalLordsGuard;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.items.Item.ItemRarity;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.DamageSource;
import net.minecraft.server.v1_9_R2.EntitySkeleton;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalAbyss extends EntitySkeleton implements Boss {

	public InfernalGhast ghast;
	public InfernalLordsGuard guard;
	
	public InfernalAbyss(World world) {
		super(world);

	}
	/**
	 * @param world
	 */
	public InfernalAbyss(World world, Location loc) {
		super(world);
		this.setSkeletonType(1);
		this.fireProof = true;
		this.setOnFire(Integer.MAX_VALUE);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = 50;
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss",
		        new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity()
		        .setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);
		for (Player p : API.getNearbyPlayers(loc, 50)) {
			p.sendMessage(this.getCustomName() + ChatColor.RESET.toString() + ": " + getEnumBoss().greeting);
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
			if (!this.getBukkitEntity().isDead())
				this.getBukkitEntity().getLocation().add(0, 1, 0).getBlock().setType(Material.FIRE);
		} , 0, 20L);
		ghast = new InfernalGhast(this);
		guard = new InfernalLordsGuard(this);
		guard.isInvulnerable(DamageSource.FALL);
		guard.setLocation(locX, locY, locZ, 1, 1);

		setArmor(getEnumBoss().tier);
	}

	protected void setArmor(int tier) {
		// weapon.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("infernalstaff")));
		this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("infernalboot")));
		this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("infernallegging")));
		this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("infernalchest")));
		this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("infernalhelmet")));
        ghast.setArmor(new ItemGenerator().setTier(ItemTier.getByTier(tier)).setRarity(ItemRarity.UNIQUE).getArmorSet(),
                new ItemGenerator().setTier(ItemTier.getByTier(tier)).setRarity(ItemRarity.UNIQUE)
                        .setType(ItemType.getRandomWeapon()).generateItem().getItem());
    }

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.InfernalAbyss;
	}

	@Override
	public void onBossDeath() {
		// Giant Explosion that deals massive damage
		if (hasFiredGhast)
			say(this.getBukkitEntity(), getEnumBoss().death);
	}

	public boolean hasFiredGhast = false;
	public boolean finalForm = false;
	
	
	@Override
	public void onBossHit(EntityDamageByEntityEvent event) {
		if(!finalForm)
		if (this.ghast.isAlive() || this.guard.isAlive()) {
			say(this.getBukkitEntity(), "Hah! You must take out my minions.");
			event.setDamage(0);
			event.setCancelled(true);
		}

		LivingEntity en = (LivingEntity) event.getEntity();
		double seventyFivePercent = HealthHandler.getInstance().getMonsterMaxHPLive(en) * 0.75;

		if (HealthHandler.getInstance().getMonsterHPLive(en) <= seventyFivePercent && !hasFiredGhast) {
			say(this.getBukkitEntity(), "Taste FIRE!");
			ghast.setLocation(this.locX, this.locY + 4, this.locZ, 1, 1);
			this.getWorld().addEntity(ghast, SpawnReason.CUSTOM);
			ghast.init();
			this.isInvulnerable(DamageSource.STUCK);
			this.setLocation(locX, locY + 100, locZ, 1, 1);
			hasFiredGhast = true;
		}
	}

}
