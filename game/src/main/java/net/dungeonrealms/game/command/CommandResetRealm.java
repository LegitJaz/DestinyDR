package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/23/2016
 */

public class CommandResetRealm extends BaseCommand {

    public CommandResetRealm(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return false;

        player.sendMessage(ChatColor.RED + "This feature is currently disabled");
        return true;
//        long lastReset = wrapper.getLastRealmReset();
//
//        if (lastReset != 0 && !Rank.isTrialGM(player)) {
//            player.sendMessage(ChatColor.RED + "You may only reset your realm " + ChatColor.UNDERLINE + "ONCE" + ChatColor.RED + " per hour.");
//            return true;
//        }
//
//        Realm realm = Realms.getInstance().getOrCreateRealm(player, wrapper.getAccountID());
//
//        if (realm.getState() != RealmState.OPENED && realm.getState() != RealmState.CLOSED) {
//        	player.sendMessage(realm.getState().getStatusMessage());
//        	return true;
//        }
//
//        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 1);
//        player.sendMessage(ChatColor.GRAY + "Are you sure you want to RESET your realm  - This cannot be undone. " + "(" + ChatColor.GREEN.toString() + ChatColor.BOLD + "Y" + ChatColor.GRAY + " / " + ChatColor.RED.toString() + ChatColor.BOLD + "N" + ChatColor.GRAY + ")");
//        player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "DISCLAIMER: " + ChatColor.GRAY + "You will not receive " + ChatColor.UNDERLINE + "ANY" + ChatColor.GRAY + " of your resources in your realm back. Your realm will be " + ChatColor.BOLD + "PERMANENTLY DELETED" + ChatColor.GRAY + ". Your realm upgrades will also be removed.");
//
//        Chat.promptPlayerConfirmation(player, () -> {
//            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Resetting your realm ...");
//            Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> realm.resetRealm(player));
//        }, () -> player.sendMessage(ChatColor.RED + "/resetrealm - " + ChatColor.BOLD + "CANCELLED"));
//        return true;
    }
}
