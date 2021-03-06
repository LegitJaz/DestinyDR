package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.mechanic.data.ProfessionTier;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * A basic fishing pole item
 * @author Kneesnap
 */
public class ItemFishingPole extends ProfessionItem {

	public ItemFishingPole() {
		this(1);
	}
	
	public ItemFishingPole(int level) {
		super(ItemType.FISHING_POLE, level);
	}
	
	public ItemFishingPole(ItemStack item) {
		super(item);
	}
	
	@Override
	public void updateItem() {
		getMeta().addEnchant(Enchantment.LURE, 3, false);
		super.updateItem();
	}


	public static boolean isFishingPole(ItemStack item) {
		return isType(item, ItemType.FISHING_POLE);
	}

	@Override
	public ProfessionTier getProfessionTier() {
		return FishingTier.getTierByLevel(getLevel());
	}
}
