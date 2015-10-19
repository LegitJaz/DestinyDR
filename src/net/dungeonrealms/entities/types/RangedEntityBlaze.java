package net.dungeonrealms.entities.types;

import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.EnumMonster;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;

/**
 * Created by Chase on Oct 4, 2015
 */
public abstract class RangedEntityBlaze extends net.minecraft.server.v1_8_R3.EntityBlaze {

	protected String name;
	protected String mobHead;
	protected EnumEntityType entityType;
	protected EnumMonster monsterType;
	public RangedEntityBlaze(World world, EnumMonster monster, int tier, EnumEntityType entityType, boolean setArmor) {
		this(world);
		monsterType = monster;
		this.name = monster.name;
		this.mobHead = monster.mobHead;
		this.entityType = entityType;
		if (setArmor)
			setArmor(tier);
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = Utils.getRandomFromTier(tier);
		MetadataUtils.registerEntityMetadata(this, this.entityType, tier, level);
		EntityStats.setMonsterRandomStats(this, level, tier);
		setStats();
		this.getBukkitEntity().setCustomName(ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] " + ChatColor.RESET
		        + monster.getPrefix() + name + monster.getSuffix());
	}

	@Override
	protected abstract Item getLoot();

	@Override
	protected abstract void getRareDrop();

	protected RangedEntityBlaze(World world) {
		super(world);
	}

	protected abstract void setStats();

	public static Object getPrivateField(String fieldName, Class clazz, Object object) {
		Field field;
		Object o = null;
		try {
			field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			o = field.get(object);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return o;
	}
	protected String getCustomEntityName() {
		return this.name;
	}
	protected net.minecraft.server.v1_8_R3.ItemStack getHead() {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner(mobHead);
		head.setItemMeta(meta);
		return CraftItemStack.asNMSCopy(head);
	}
	/**
	 * set monster armor tier.
	 * @param tier
	 */
	private void setArmor(int tier) {
		ItemStack[] armor = getTierArmor(tier);
		// weapon, boots, legs, chest, helmet/head
		ItemStack weapon = getTierWeapon(tier);
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
		this.setEquipment(4, this.getHead());
	}

	private ItemStack getTierWeapon(int tier) {
		return new ItemGenerator().next(net.dungeonrealms.items.Item.ItemType.STAFF, net.dungeonrealms.items.Item.ItemTier.getByTier(tier));
		// TODO: MAKE THIS TAKE A TIER AND BASE IT ON THAT. DO THE SAME WITH
		// ARMOR DON'T JUST CREATE NEW SHITTY BUKKIT ONES.
		/*
		 * if (tier == 1) { return new ItemStack(Material.WOOD_SWORD, 1); } else
		 * if (tier == 2) { return new ItemStack(Material.STONE_SWORD, 1); }
		 * else if (tier == 3) { return new ItemStack(Material.IRON_SWORD, 1); }
		 * else if (tier == 4) { return new ItemStack(Material.DIAMOND_SWORD,
		 * 1); } else if (tier == 5) { return new ItemStack(Material.GOLD_SWORD,
		 * 1); } return new ItemStack(Material.WOOD_SWORD, 1);
		 */
	}


	/**
	 * get monster tier Armor as ItemsStack array
	 * 
	 * @param tier
	 * @return
	 */
	private ItemStack[] getTierArmor(int tier) {
		return new ArmorGenerator().nextTier(tier);
	}

	@Override
	protected String z() {
		return "";
	}

	@Override
	protected String bo() {
		return "game.player.hurt";
	}

	@Override
	protected String bp() {
		return "mob.ghast.scream";
	}
}
