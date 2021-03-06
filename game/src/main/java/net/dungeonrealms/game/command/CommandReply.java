package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.util.ChatUtil;
import net.dungeonrealms.database.punishment.PunishAPI;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.player.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Alan on 7/25/2016.
 */
public class CommandReply extends BaseCommand {

    public CommandReply(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (PunishAPI.isMuted(player.getUniqueId())) {
            player.sendMessage(PunishAPI.getMutedMessage(player.getUniqueId()));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "/r <message>");
            return true;
        }

        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null) return true;

        if (gp.getLastMessager() == null) {
            player.sendMessage(ChatColor.RED + "No player has messaged you recently.");
            return true;
        }

        String msg = args[0];
        for (int i = 1; i < args.length; i++) {
            msg += " " + args[i];
        }

        if(ChatUtil.containsIllegal(msg)){
            player.sendMessage(ChatColor.RED + "Message contains illegal characters.");
            return true;
        }

        Chat.sendPrivateMessage(player, gp.getLastMessager(), msg);

        return true;
    }
}
