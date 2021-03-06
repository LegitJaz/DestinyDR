package net.dungeonrealms.game.listener.inventory;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.UpdateType;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.ChestEffectsGUI;
import net.dungeonrealms.game.world.shops.Shop;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.shops.SoldShopItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Chase on Sep 23, 2015
 */
public class ShopListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void playerOpenShopInventory(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        Player p = event.getPlayer();

        if (block == null || block.getType() != Material.CHEST || p.isSneaking())
            return;

        Shop shop = ShopMechanics.getShop(block);
        if (shop == null || Metadata.PRICING.has(p) || Metadata.SHARDING.has(p)) // Don't allow people buying items or sharding to open it.
            return;

        event.setCancelled(true);

        if (!GameAPI.isNonPvPRegion(p.getLocation())) {
            p.sendMessage(ChatColor.RED + "You cannot access a shop in a " + ChatColor.BOLD + "CHAOTIC" + ChatColor.RED + " region!");
            return;
        }

        if (Chat.listened(p)) {
            p.sendMessage(ChatColor.RED + "You can't interact with inventories whilst being interactive with chat");
            return;
        }

        if (shop.ownerName.equals(event.getPlayer().getName()) || Rank.isTrialGM(event.getPlayer()) || shop.isopen) {
            p.openInventory(shop.getInventory());
            p.playSound(event.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
            p.setCanPickupItems(false);
        }
    }

    @EventHandler
    public void playerCloseShopInventory(InventoryCloseEvent event) {
        if (GameAPI.isShop(event.getInventory()))
            event.getPlayer().setCanPickupItems(true);
    }

    /**
     * Handling Shops being Right clicked.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void playerAttemptShopUpgrade(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.CHEST) return;
        Shop shop = ShopMechanics.getShop(block);
        if (shop == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (!shop.isopen) {
            if (event.getPlayer().isSneaking()) {
                if (shop.ownerUUID.toString().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
                    event.setCancelled(true);
                    shop.promptUpgrade();
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "You do not own this shop.");
                }
            }
        } else {
            if (event.getPlayer().isSneaking()) {
                if (shop.ownerUUID.toString().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "You must close your shop to upgrade it.");
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerClickShopInventory(InventoryClickEvent event) {
        if (!GameAPI.isShop(event.getInventory())) return;

        String ownerName = event.getInventory().getTitle().split("@")[1];
        if (ownerName == null) return;
        Shop shop = ShopMechanics.getShop(ownerName);
        if (shop == null) return;

        // Prevents Stealing from shops. //
        if (event.getAction() == InventoryAction.NOTHING || event.getAction() == InventoryAction.UNKNOWN) {
            event.setCancelled(true);
            return;
        }

        Player clicker = (Player) event.getWhoClicked();

        //  PREVENT CERTAIN CLICKTYPES  //
        if (event.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD)
                || event.getAction().equals(InventoryAction.HOTBAR_SWAP)
                || event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
            event.setCancelled(true);
            return;
        }

        if (clicker.getUniqueId().toString().equalsIgnoreCase(shop.ownerUUID.toString()) || Rank.isTrialGM(clicker)) {
            // Owner is Clicking

            if (event.getRawSlot() == (shop.getInvSize() - 1)) {
                //Toggle open / closed status.
                event.setCancelled(true);
                clicker.playSound(event.getWhoClicked().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                shop.updateStatus();
                return;
            }

            if (event.getRawSlot() == shop.getInvSize() - 2) {
                //Delete Shop
                event.setCancelled(true);
                shop.deleteShop(false, null);
                return;
            }

            if (event.getRawSlot() == shop.getInvSize() - 5) {
                //Description
                event.setCancelled(true);
                clicker.sendMessage(ChatColor.GREEN + "Please enter your new shop description!");
                Chat.listenForMessage(clicker, evt -> {
                    if (evt.getMessage().equals("cancel")) {
                        clicker.sendMessage(ChatColor.RED + "Setting Shop Description - " + ChatColor.BOLD + "CANCELLED");
                        return;
                    }
                    shop.setDescription(evt.getMessage());
                    clicker.sendMessage(ChatColor.GREEN + "Your shop description is now: " + ChatColor.GRAY + evt.getMessage());
                });
                return;
            }

            if (event.getRawSlot() == shop.getInvSize() - 8) {
                //Description
                event.setCancelled(true);
                /*clicker.sendMessage(ChatColor.GREEN + "Please enter your new shop description!");
                Chat.listenForMessage(clicker, evt -> {
                    if (evt.getMessage().equals("cancel")) {
                        clicker.sendMessage(ChatColor.RED + "Setting Shop Description - " + ChatColor.BOLD + "CANCELLED");
                        return;
                    }
                    shop.setDescription(evt.getMessage());
                    clicker.sendMessage(ChatColor.GREEN + "Your shop description is now: " + ChatColor.GRAY + evt.getMessage());
                });*/
                clicker.closeInventory();
                new ChestEffectsGUI(clicker).open(clicker,event.getAction());
                return;
            }

            if (event.getRawSlot() >= (shop.getInvSize() - 9) && event.getRawSlot() < shop.getInvSize()) {
                event.setCancelled(true);
                return;
            }

            ItemStack itemHeld = event.getCursor();
            ItemStack stackInSlot = event.getCurrentItem();
            if (shop.isopen) {
                clicker.sendMessage(ChatColor.RED + "You must close the shop before you can edit");
                event.setCancelled(true);
                return;
            }

            if (event.isShiftClick()) {
                if (event.getRawSlot() >= event.getInventory().getSize()) {

                    if (!ShopMechanics.isItemSellable(stackInSlot)) {
                        event.setCancelled(true);
                        clicker.sendMessage(ChatColor.RED + "You cannot sell this item!");
                        return;
                    }

                    ItemStack stackClicked = event.getCurrentItem().clone();
                    event.setCurrentItem(new ItemStack(Material.AIR));
                    addItemToShop(clicker, shop, stackClicked);
                } else {
                    event.setCurrentItem(removePrice(event.getCurrentItem()));
                }
                return;
            }

            if (stackInSlot != null && stackInSlot.getType() != Material.AIR && itemHeld.getType() != Material.AIR && itemHeld.getType() != stackInSlot.getType()) {
                clicker.sendMessage(ChatColor.RED.toString() + "Move item in slot first.");
                event.setCancelled(true);
            } else {
                if (event.getRawSlot() >= event.getInventory().getSize())
                    return;
                if (event.isLeftClick()) {
                    if (stackInSlot == null || stackInSlot.getType() == Material.AIR && itemHeld.getType() != Material.AIR) {

                        if (!ShopMechanics.isItemSellable(itemHeld)) {
                            event.setCancelled(true);
                            clicker.sendMessage(ChatColor.RED + "You cannot sell this item!");
                            return;
                        }

                        event.setCancelled(true);
                        event.setCursor(new ItemStack(Material.AIR));
                        addItemToShop(clicker, shop, itemHeld);
                        return;
                    }

                    if (event.getWhoClicked().getInventory().firstEmpty() < 0) {
                        event.getWhoClicked().sendMessage(ChatColor.RED + "Make room in your inventory.");
                        event.setCancelled(true);
                        return;
                    }

                    //  OWNER REMOVES ITEM FROM SHOP  //
                    clicker.getInventory().addItem(removePrice(stackInSlot));
                    event.getInventory().setItem(event.getRawSlot(), new ItemStack(Material.AIR, 1));

                } else if (event.isRightClick()) {

                    //  CANT CHANGE PRICE OF AN ITEM THAT DOESNT EXIST  //
                    if (stackInSlot == null || stackInSlot.getType() == Material.AIR) {
                        event.setCancelled(true);
                        return;
                    }

                    event.setCancelled(true);
                    clicker.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "GEM" + ChatColor.GREEN + " value of [" + ChatColor.BOLD + "1x" + ChatColor.GREEN + "] of this item.");
                    Chat.listenForNumber(clicker, 1, Integer.MAX_VALUE / 64, price -> {

                        if (clicker.getLocation().distanceSquared(shop.block1.getLocation()) > 16) {
                            clicker.sendMessage(ChatColor.RED + "You are too far away from the shop [>4 blocks], addition of item CANCELLED.");
                            return;
                        }

                        //  CHANGES THE ITEM PRICE  //
                        VanillaItem i = new VanillaItem(shop.inventory.getItem(event.getRawSlot()));
//                        i.updateItem();
                        i.setPrice(price);
                        shop.inventory.setItem(event.getRawSlot(), i.generateItem());
                        clicker.playSound(clicker.getLocation(), Sound.ENTITY_ARROW_HIT, 1, 1);
                    }, () -> clicker.sendMessage(ChatColor.RED + "Action cancelled."));
                }
            }
        } else {
            event.setCancelled(true);
            // Not Owner Clicking

            if (!shop.isopen) {
                if (event.getCursor() != null) {
                    clicker.getInventory().addItem(event.getCursor());
                    event.setCursor(new ItemStack(Material.AIR));
                }
                clicker.closeInventory();
                clicker.sendMessage(ChatColor.RED + "The shop has closed");
                return;
            }

            if (event.getRawSlot() >= (shop.getInvSize() - 9))
                return;

            final ItemStack itemClicked = event.getCurrentItem();
            if (itemClicked == null || itemClicked.getType() == Material.AIR) return;

            if (clicker.getInventory().firstEmpty() == -1) {
                clicker.sendMessage(ChatColor.RED + "No space available in inventory. Clear some room before attempting to purchase.");
                return;
            }

            boolean shiftClick = event.isShiftClick();
            if (!Metadata.PRICING.has(clicker)) {
                int itemPrice = new VanillaItem(itemClicked).getPrice();
                if (itemPrice <= 0)
                    return;

                if (!shiftClick) {
                    Metadata.PRICING.set(clicker, true);
                    clicker.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "QUANTITY" + ChatColor.GREEN + " you'd like to purchase.");
                    clicker.sendMessage(ChatColor.GRAY + "MAX: " + itemClicked.getAmount() + "X (" + itemPrice * itemClicked.getAmount() + "g), OR " + itemPrice + "g/each.");
                    Chat.listenForNumber(clicker, 1, 64, quantity -> {
                        Metadata.PRICING.remove(clicker);

                        //  PREVENT PURCHASING FROM AN INVALID SHOP  //
                        if (!ShopMechanics.ALLSHOPS.containsKey(ownerName) || !shop.isopen ||
                                !(ShopMechanics.ALLSHOPS.get(ownerName).equals(shop))) {
                            clicker.sendMessage(ChatColor.RED + "The shop is no longer available.");
                            return;
                        }

                        //  MAKE SURE THE ITEM WE'RE TRYING TO BUY WASN'T CHANGED (Dupe)  //
                        if (shop.getInventory().getItem(event.getRawSlot()) == null || !shop.getInventory().getItem(event.getRawSlot()).equals(itemClicked)) {
                            clicker.sendMessage(ChatColor.RED + "That item is no longer available.");
                            return;
                        }

                        //  MAKE SURE WE'RE BUYING A VALID QUANTITY  //
                        if (quantity > itemClicked.getAmount()) {
                            clicker.sendMessage(ChatColor.RED + "There are only [" + ChatColor.BOLD + itemClicked.getAmount() + ChatColor.RED + "] available.");
                            return;
                        }

                        attemptPurchaseItem(clicker, shop, event.getRawSlot(), itemClicked, quantity);
                    }, () -> {
                        Metadata.PRICING.remove(clicker);
                        clicker.sendMessage(ChatColor.RED + "Purchase of item " + ChatColor.BOLD + "CANCELLED");
                    });
                } else if (event.isShiftClick()) {
                    attemptPurchaseItem(clicker, shop, event.getRawSlot(), itemClicked, itemClicked.getAmount());
                }
            } else {
                // Not possible anymore, prevented a dupe.
            }
        }
    }

    private void addItemToShop(Player player, Shop shop, ItemStack item) {
        cancelPricingItem(player);

        BankMechanics.shopPricing.put(player.getName(), item);
        player.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "GEM" + ChatColor.GREEN + " value of [" + ChatColor.BOLD + "1x" + ChatColor.GREEN + "] of this item.");

        Chat.listenForNumber(player, 1, 1_000_000, price -> {
            if (!player.getWorld().equals(shop.block1.getWorld()) || player.getLocation().distanceSquared(shop.block1.getLocation()) > 16) {
                player.sendMessage(ChatColor.RED + "You are too far away from the shop [>4 blocks], addition of item CANCELLED.");
                cancelPricingItem(player);
                return;
            }

            if (BankMechanics.shopPricing.get(player.getName()) == null) return;
            if (shop.inventory.firstEmpty() >= 0 && shop.inventory.firstEmpty() < (shop.inventory.getSize() - 9)) {
                int slot = shop.inventory.firstEmpty();

                VanillaItem i = new VanillaItem(BankMechanics.shopPricing.get(player.getName()));
                i.setPrice(price);
                shop.inventory.setItem(slot, i.generateItem());

                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, 1, 1);

                player.sendMessage(new String[]{
                        ChatColor.GREEN + "Price set. Right-Click item to edit.",
                        ChatColor.YELLOW + "Left Click the item to remove it from your shop."});

                BankMechanics.shopPricing.remove(player.getName());
                player.updateInventory();
            } else {
                cancelPricingItem(player);
                player.sendMessage("There is no room for this item in your Shop");
            }
        }, () -> {
            player.sendMessage(ChatColor.RED + "Pricing of item - " + ChatColor.BOLD + "CANCELLED");
            cancelPricingItem(player);
        });
    }

    private void cancelPricingItem(Player player) {
        if (!BankMechanics.shopPricing.containsKey(player.getName()))
            return;
        player.getInventory().addItem(BankMechanics.shopPricing.get(player.getName()));
        BankMechanics.shopPricing.remove(player.getName());
        player.updateInventory();
    }

    private void attemptPurchaseItem(Player player, Shop shop, int rawSlot, ItemStack item, int quantity) {

        //  PREVENT PURCHASING IF NO FREE SPACE  //
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "No space available in inventory. Please clear some room.");
            return;
        }

        //  ARE WE CLOSE ENOUGH  //
        if (player.getLocation().distanceSquared(shop.block1.getLocation()) > 16) {
            player.sendMessage(ChatColor.RED + "You are too far away from the shop [>4 blocks], addition of item CANCELLED.");
            cancelPricingItem(player);
            return;
        }

        //  MAKE SURE WE CAN PAY FOR THIS  //
        int itemPrice = new VanillaItem(item).getPrice();

        int totalPrice = quantity * itemPrice;
        if (totalPrice > 0 && (BankMechanics.getGemsInInventory(player) < totalPrice)) {
            player.sendMessage(ChatColor.RED + "You do not have enough GEM(s) to complete this purchase.");
            player.sendMessage(ChatColor.GRAY + "" + quantity + " X " + itemPrice + " gem(s)/ea = " + totalPrice + " gem(s).");
            return;
        }

        //  DONT ALLOW BUYING ITEMS WHILE SHARDING  //
        if (Metadata.SHARDING.has(player)) {
            player.sendMessage(ChatColor.RED + "You cannot purchase an item while sharding.");
            return;
        }

        //  GIVE THE ITEM TO THE BUYER  //
        BankMechanics.takeGemsFromInventory(player, totalPrice);
        ItemStack toGive = removePrice(item.clone());
        toGive.setAmount(quantity);
        player.getInventory().addItem(toGive);
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "-" + ChatColor.RED + totalPrice + ChatColor.BOLD + "G");
        player.sendMessage(ChatColor.GREEN + "Transaction successful.");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
        player.updateInventory();
        shop.addPurchaseHistory(Utils.getItemName(toGive), totalPrice, quantity);

        //  REMOVE THE ITEM FROM THE SHOP  //
        int remainingStock = item.getAmount() - quantity;
        if (remainingStock > 0) {
            item.setAmount(remainingStock);
        } else {
            shop.inventory.clear(rawSlot);
        }

        //  GIVE THE SELLER WHAT THEY'VE EARNED  //
        BungeeUtils.sendPlayerMessage(shop.ownerName, ChatColor.GREEN + "SOLD " + quantity + "x '" + Utils.getItemName(item) + ChatColor.GREEN + "' for " + ChatColor.BOLD + totalPrice + "g" + ChatColor.GREEN + " to " + ChatColor.WHITE + "" + ChatColor.BOLD + player.getName());

        ShopMechanics.getRecentlySoldItems().add(new SoldShopItem(shop.ownerUUID, shop.ownerName, toGive, totalPrice, player.getName()));

        Player onlineOwner = shop.getOwner();
        if (onlineOwner != null) {
            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(shop.getOwner());
            if (wrapper != null) {
                wrapper.addGems(totalPrice);
                wrapper.getPlayerGameStats().addStat(StatColumn.GEMS_EARNED, totalPrice);
            }

            Achievements.giveAchievement(shop.getOwner(), EnumAchievements.SHOP_MERCHANT);
        } else {
            SQLDatabaseAPI.getInstance().executeBatch((result) -> GameAPI.updatePlayerData(shop.ownerUUID, UpdateType.GEMS),
                    QueryType.INCREMENT_GEMS.getQuery(totalPrice, shop.getCharacterID()), QueryType.INCREMENT_GEMS_EARNED.getQuery(totalPrice, shop.getCharacterID()));
        }

        //  CALCULATE HOW MANY ITEMS ARE LEFT  //
        int itemsLeft = 0;
        for (int i = 0; i < shop.inventory.getContents().length - 9; i++)
            if (shop.inventory.getContents()[i] != null && shop.inventory.getContents()[i].getType() != Material.AIR)
                itemsLeft++;

        //  REMOVE SHOP IF EMPTY  //
        if (itemsLeft == 0) {
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
                if (shop.isopen) {
                    shop.deleteShop(false, null);
                    BungeeUtils.sendPlayerMessage(shop.ownerName, ChatColor.GREEN + "Your shop on " +
                            DungeonRealms.getInstance().bungeeName + " has " + ChatColor.RED + ChatColor.BOLD + "SOLD OUT" + ChatColor.GREEN + " and has been removed to free space.");
                }
            }, 3L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerOpenShopInventory(InventoryOpenEvent event) {
        if (!GameAPI.isShop(event.getInventory())) return;
        String ownerName = event.getInventory().getTitle().split("@")[1];
        if (ownerName == null) return;
        Shop shop = ShopMechanics.getShop(ownerName);
        if (shop == null) return;
        if (!shop.isopen) return;
        if (event.getPlayer().getName().equalsIgnoreCase(ownerName)) return;
        if (shop.uniqueViewers.contains(event.getPlayer().getName())) return;
        shop.viewCount = shop.viewCount + 1;
        shop.uniqueViewers.add(event.getPlayer().getName());
        shop.hologram.removeLine(1);
        shop.hologram.insertTextLine(1, String.valueOf(shop.viewCount) + ChatColor.RED + Shop.HEART);
    }

    public ItemStack removePrice(ItemStack item) {
        VanillaItem i = new VanillaItem(item);
        i.removePrice();
        return i.generateItem();
    }
}