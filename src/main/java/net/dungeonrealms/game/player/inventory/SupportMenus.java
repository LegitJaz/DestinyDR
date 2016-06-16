package net.dungeonrealms.game.player.inventory;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.rank.Rank;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Brad on 16/06/2016.
 */
public class SupportMenus {

    private static ItemStack applySupportItemTags(ItemStack item, String playerName, UUID uuid) {
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("name", new NBTTagString(playerName));
        tag.set("uuid", new NBTTagString(uuid.toString()));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static void openMainMenu(Player player, String playerName) {
        try {
            UUID uuid = Bukkit.getPlayer(playerName) != null && Bukkit.getPlayer(playerName).getDisplayName().equalsIgnoreCase(playerName) ? Bukkit.getPlayer(playerName).getUniqueId() : UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
            DatabaseAPI.getInstance().requestPlayer(uuid);
            String playerRank = Rank.getInstance().getRank(uuid);
            if (!Rank.isDev(player) && (playerRank.equalsIgnoreCase("gm") || playerRank.equalsIgnoreCase("dev"))) {
                player.sendMessage(ChatColor.RED + "You " + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "DO NOT" + ChatColor.RED + " have permission to manage this user.");
                return;
            }

            ItemStack item;

            Inventory inv = Bukkit.createInventory(null, 45, "Support Tools");

            item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                    ChatColor.WHITE + "Rank: " + Rank.rankFromPrefix(playerRank),
                    ChatColor.WHITE + "Level: " + DatabaseAPI.getInstance().getData(EnumData.LEVEL, uuid),
                    ChatColor.WHITE + "Experience: " + DatabaseAPI.getInstance().getData(EnumData.EXPERIENCE, uuid),
                    ChatColor.WHITE + "E-Cash: " + DatabaseAPI.getInstance().getData(EnumData.ECASH, uuid),
                    ChatColor.WHITE + "Bank Balance: " + DatabaseAPI.getInstance().getData(EnumData.GEMS, uuid),
                    ChatColor.WHITE + "Hearthstone Location: " + DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid),
                    ChatColor.WHITE + "Alignment: " + Utils.ucfirst(DatabaseAPI.getInstance().getData(EnumData.ALIGNMENT, uuid).toString())
            });
            inv.setItem(4, applySupportItemTags(item, playerName, uuid));

            // Rank Manager
            if (!playerName.equalsIgnoreCase(player.getDisplayName())) {
                item = editItem(new ItemStack(Material.DIAMOND), ChatColor.GOLD + "Rank Manager", new String[]{
                        ChatColor.WHITE + "Modify the rank of " + playerName + ".",
                        ChatColor.WHITE + "Current rank: " + Rank.rankFromPrefix(playerRank)
                });
            } else {
                item = editItem(new ItemStack(Material.BARRIER), ChatColor.RED + "Rank Manager", new String[]{
                   ChatColor.RED + "You cannot change the rank of your own profile."
                });
            }
            inv.setItem(19, applySupportItemTags(item, playerName, uuid));

            // Level Manager
            item = editItem(new ItemStack(Material.EXP_BOTTLE), ChatColor.GOLD + "Level Manager", new String[]{
                    ChatColor.WHITE + "Manage the level/experience of " + playerName + ".",
                    ChatColor.WHITE + "Current level: " + DatabaseAPI.getInstance().getData(EnumData.LEVEL, uuid),
                    ChatColor.WHITE + "Current EXP: " + DatabaseAPI.getInstance().getData(EnumData.EXPERIENCE, uuid)
            });
            inv.setItem(22, applySupportItemTags(item, playerName, uuid));

            // E-Cash Manager
            item = editItem(new ItemStack(Material.GOLDEN_CARROT), ChatColor.GOLD + "E-Cash Manager", new String[]{
                    ChatColor.WHITE + "Manage the e-cash of " + playerName + ".",
                    ChatColor.WHITE + "Current E-Cash: " + DatabaseAPI.getInstance().getData(EnumData.ECASH, uuid)
            });
            inv.setItem(25, applySupportItemTags(item, playerName, uuid));

            // Bank Manager
            item = editItem(new ItemStack(Material.ENDER_CHEST), ChatColor.GOLD + "Bank Manager", new String[]{
                    ChatColor.WHITE + "Manage the bank of " + playerName + ".",
                    ChatColor.WHITE + "Current bank balance: " + DatabaseAPI.getInstance().getData(EnumData.GEMS, uuid)
            });
            inv.setItem(28, applySupportItemTags(item, playerName, uuid));

            // Hearthstone Manager
            item = editItem(new ItemStack(Material.QUARTZ_ORE), ChatColor.GOLD + "Hearthstone Manager", new String[]{
                    ChatColor.WHITE + "Manage the Hearthstone Location of " + playerName + ".",
                    ChatColor.WHITE + "Current location: " + DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid)
            });
            inv.setItem(31, applySupportItemTags(item, playerName, uuid));

            // Shop Packages
            item = editItem(new ItemStack(Material.BOOK_AND_QUILL), ChatColor.GOLD + "Cosmetics", new String[]{
                    ChatColor.WHITE + "Manage cosmetics of " + playerName + "."
            });
            inv.setItem(34, applySupportItemTags(item, playerName, uuid));

            // PLACEHOLDER
            /*item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "PLACEHOLDER", new String[]{
                    ChatColor.WHITE + "This is a placeholder, it does nothing.",
                    "",
                    ChatColor.WHITE + "One day, a tool for support will go here."
            });
            inv.setItem(34, applySupportItemTags(item, playerName, uuid));*/

            player.openInventory(inv);
        } catch (IllegalArgumentException ex) {
            // This exception is thrown if the UUID doesn't exist in the database.
            player.sendMessage(ChatColor.RED + "Unable to identify anybody with the player name: " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.RED + "!");
            player.sendMessage(ChatColor.RED + "It's likely the user has never played on the Dungeon Realms servers before.");
        }
    }

    public static void openRankMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Rank)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.GRAY.getData()), Rank.rankFromPrefix("default"), new String[]{
                ChatColor.WHITE + "Set user rank to: Default"
        });
        inv.setItem(20, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), Rank.rankFromPrefix("sub"), new String[]{
                ChatColor.WHITE + "Set user rank to: Subscriber",
                ChatColor.WHITE + "User will have access to the subscriber server."
        });
        inv.setItem(21, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.ORANGE.getData()), Rank.rankFromPrefix("sub+"), new String[]{
                ChatColor.WHITE + "Set user rank to: Subscriber+",
                ChatColor.WHITE + "User will have access to the subscriber server."
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.CYAN.getData()), Rank.rankFromPrefix("sub++"), new String[]{
                ChatColor.WHITE + "Set user rank to: Subscriber++",
                ChatColor.WHITE + "User will have access to the subscriber server."
        });
        inv.setItem(23, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.WHITE.getData()), Rank.rankFromPrefix("pmod"), new String[]{
                ChatColor.WHITE + "Set user rank to: Player Moderator",
                ChatColor.WHITE + "User will have access to the subscriber server.",
                ChatColor.WHITE + "User will have access to limited moderation tools."
        });
        inv.setItem(24, applySupportItemTags(item, playerName, uuid));

        // Ranks that can only be applied by developers.
        if (Rank.isDev(player)) {
            item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.CYAN.getData()), Rank.rankFromPrefix("builder"), new String[]{
                    ChatColor.WHITE + "Set user rank to: Builder",
                    ChatColor.WHITE + "User will have identical permissions as a Subscriber."
            });
            inv.setItem(29, applySupportItemTags(item, playerName, uuid));

            item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()), Rank.rankFromPrefix("youtube"), new String[]{
                    ChatColor.WHITE + "Set user rank to: YouTuber",
                    ChatColor.WHITE + "User will have identical permissions as a Subscriber.",
                    ChatColor.WHITE + "User will have access to a special 'YouTube' server."
            });
            inv.setItem(30, applySupportItemTags(item, playerName, uuid));

            item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData()), Rank.rankFromPrefix("support"), new String[]{
                    ChatColor.WHITE + "Set user rank to: Support Agent",
                    ChatColor.WHITE + "User will " + ChatColor.BOLD + "NOT" + ChatColor.WHITE + " have access to moderation tools.",
                    ChatColor.WHITE + "User will have access to a special command set.",
                    ChatColor.WHITE + "User will have access a special 'Support' server."
            });
            inv.setItem(32, applySupportItemTags(item, playerName, uuid));

            item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.LIGHT_BLUE.getData()), Rank.rankFromPrefix("gm"), new String[]{
                    ChatColor.WHITE + "Set user rank to: Game Master",
                    ChatColor.WHITE + "User will " + ChatColor.BOLD + "NOT" + ChatColor.WHITE + " have access to support tools.",
                    ChatColor.WHITE + "User will have access to almost all commands.",
                    ChatColor.WHITE + "User will have access to the MASTER server."
            });
            inv.setItem(33, applySupportItemTags(item, playerName, uuid));
        }

        player.openInventory(inv);
    }

    public static void openLevelMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Level)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "PLACEHOLDER", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openECashMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (E-Cash)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "PLACEHOLDER", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openBankMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Bank)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "PLACEHOLDER", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openHearthstoneMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Hearthstone)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("cyrennica") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Cyrennica", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Cyrennica"
        });
        inv.setItem(18, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("harrison_field") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Harrison Fields", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Harrison Fields"
        });
        inv.setItem(19, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("dark_oak") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Dark Oak Tavern", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Dark Oak Tavern"
        });
        inv.setItem(20, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("gloomy_hollows") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Gloomy Hollows", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Gloomy Hollows"
        });
        inv.setItem(21, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("tripoli") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Tripoli", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Tripoli"
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("trollsbane") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Trollsbans Tavern", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Trollsbane Tavern"
        });
        inv.setItem(23, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("crestguard") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Crestguard Keep", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Crestguard Keep"
        });
        inv.setItem(24, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("deadpeaks") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Deadpeaks Mountain", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Deadpeaks Mountain"
        });
        inv.setItem(25, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openCosmeticsMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Cosmetics)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        // Rank Manager
        item = editItem(new ItemStack(Material.EYE_OF_ENDER), ChatColor.GOLD + "Trail Manager", new String[]{
                ChatColor.WHITE + "Modify trails of " + playerName + "."
        });
        inv.setItem(19, applySupportItemTags(item, playerName, uuid));

        // Level Manager
        item = editItem(new ItemStack(Material.SADDLE), ChatColor.GOLD + "Mount / Mule Manager", new String[]{
                ChatColor.WHITE + "Manage mounts / mules of " + playerName + "."
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        // E-Cash Manager
        item = editItem(new ItemStack(Material.NAME_TAG), ChatColor.GOLD + "Pet Manager", new String[]{
                ChatColor.WHITE + "Manage pets of " + playerName + "."
        });
        inv.setItem(25, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openTrailsMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Trails)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "Trail", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(18, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openMountsMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Mounts)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "Mount", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(18, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openPetsMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Pets)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "Pet", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(18, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    private static ItemStack editItem(String playerName, String name, String[] lore) {
        return PlayerMenus.editItem(playerName, name, lore);
    }

    private static ItemStack editItem(ItemStack itemStack, String name, String[] lore) {
        return PlayerMenus.editItem(itemStack, name, lore);
    }

}
