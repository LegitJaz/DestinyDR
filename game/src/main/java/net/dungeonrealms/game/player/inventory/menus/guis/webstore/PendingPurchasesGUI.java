package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Rar349 on 5/13/2017.
 */
public class PendingPurchasesGUI extends GUIMenu {

    public PendingPurchasesGUI(Player player, int size) {
        super(player,fitSize(size), player.getName() + "'s Pending Purchases");
    }

    @Override
    protected void setItems() {
        int slot = 0;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;

        List<PendingPurchaseable> pendingItems = new ArrayList<>();
        pendingItems.addAll(wrapper.getPendingPurchaseablesUnlocked());

        pendingItems.sort(new Comparator<PendingPurchaseable>() {
            @Override
            public int compare(PendingPurchaseable o1, PendingPurchaseable o2) {
                return o1.getTimePurchased().compareTo(o2.getTimePurchased());
            }
        });

        for(PendingPurchaseable item : pendingItems) {
            List<String> lore = new ArrayList<>();
            lore.addAll(item.getPurchaseables().getDescription());
            lore.add("");
            lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "AMOUNT: " + ChatColor.GREEN + ChatColor.UNDERLINE.toString() + item.getNumberPurchased());
            lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "PURCHASED BY: " + ChatColor.GREEN + ChatColor.UNDERLINE.toString() + item.getWhoPurchased());
            lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "PURCHASED: " + ChatColor.GREEN + ChatColor.UNDERLINE.toString() + item.getTimePurchased());
            lore.add("");
            lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "LEFT CLICK TO CONFIRM");
            lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "RIGHT CLICK TO DENY");
            GUIItem pendingItem = new GUIItem(item.getPurchaseables().getItemType()).setName(item.getPurchaseables().getName(true)).setLore(lore);
            pendingItem.setClick((evt) -> {
                if(evt.getClick() == ClickType.LEFT) {

                    Utils.sendCenteredMessage(player, ChatColor.DARK_GRAY + "***" + ChatColor.GREEN.toString() +
                            ChatColor.BOLD + item.getPurchaseables().getName(false).toUpperCase() + " CLAIM CONFIRMATION" + ChatColor.DARK_GRAY + "***");
                    player.sendMessage(ChatColor.GOLD + "Are you sure you want to claim this purchase? This cannot be undone!");

                    player.sendMessage("");
                        player.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "(" + ChatColor.RED + ChatColor.BOLD.toString() + "!" + ChatColor.YELLOW + ChatColor.BOLD.toString() + ")" + ChatColor.DARK_RED + ChatColor.BOLD.toString() + " WARNING!" + ChatColor.RED + " Claiming items you did not purchase could result in a " + ChatColor.DARK_RED + ChatColor.BOLD.toString() + "PERMANANT BAN!");
                        player.sendMessage(ChatColor.RED + "If you claim this and the buyer charges back you will be" + ChatColor.DARK_RED + ChatColor.BOLD.toString() + " PERMANANTLY BANNED");

                    player.sendMessage(ChatColor.GRAY + "Type '" + ChatColor.GREEN + "Y" + ChatColor.GRAY + "' to confirm, or any other message to cancel.");
                    player.sendMessage("");
                    Chat.promptPlayerConfirmation(player, () -> {
                        boolean didRemove = wrapper.getPendingPurchaseablesUnlocked().remove(item);
                        if(!didRemove) {
                            player.sendMessage(ChatColor.RED + "Oops! Something went wrong, sorry! Please try again!");
                            return;
                        }
                        item.getPurchaseables().addNumberUnlocked(wrapper,item.getNumberPurchased());
                        player.sendMessage(ChatColor.GREEN + "Successfully claimed " + item.getNumberPurchased() + " " + item.getPurchaseables().getName() + " from " + item.getWhoPurchased());
                    }, () -> {
                        player.sendMessage(ChatColor.RED + item.getPurchaseables().getName(false).toUpperCase() + " CLAIMING - CANCELLED");
                    });
                } else if(evt.getClick() == ClickType.RIGHT) {
                    Utils.sendCenteredMessage(player, ChatColor.DARK_GRAY + "***" + ChatColor.GREEN.toString() +
                            ChatColor.BOLD + item.getPurchaseables().getName(false).toUpperCase() + " DENY CONFIRMATION" + ChatColor.DARK_GRAY + "***");
                    player.sendMessage(ChatColor.GOLD + "Are you sure you want to deny this purchase? This cannot be undone!");

                    player.sendMessage("");
                    player.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "(" + ChatColor.RED + ChatColor.BOLD.toString() + "!" + ChatColor.YELLOW + ChatColor.BOLD.toString() + ")" + ChatColor.DARK_RED + ChatColor.BOLD.toString() + " WARNING!" + ChatColor.RED + " Denying pending purchases is permanent! Your purchase will NOT be refunded!");
                    player.sendMessage(ChatColor.RED + "If you deny this you can never get it back!");

                    player.sendMessage(ChatColor.GRAY + "Type '" + ChatColor.GREEN + "Y" + ChatColor.GRAY + "' to confirm, or any other message to cancel.");
                    player.sendMessage("");
                    Chat.promptPlayerConfirmation(player, () -> {
                        boolean didRemove = wrapper.getPendingPurchaseablesUnlocked().remove(item);
                        if(!didRemove) {
                            player.sendMessage(ChatColor.RED + "Oops! Something went wrong, sorry! Please try again!");
                            return;
                        }
                        SQLDatabaseAPI.getInstance().executeUpdate(null, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(),wrapper.getAccountID()));
                        player.sendMessage(ChatColor.GREEN + "Successfully denied " + item.getNumberPurchased() + " " + item.getPurchaseables().getName() + " from " + item.getWhoPurchased());
                    }, () -> {
                        player.sendMessage(ChatColor.RED + item.getPurchaseables().getName(false).toUpperCase() + " CLAIMING - CANCELLED");
                    });
                }
            });
            setItem(slot++, pendingItem);
        }
    }
}