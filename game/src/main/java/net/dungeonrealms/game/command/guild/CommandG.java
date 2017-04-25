package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/16/2016
 */
public class CommandG extends BaseCommand {

    public CommandG(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        GuildWrapper wrapper = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());

        if (wrapper == null) {
            player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to use " + ChatColor.BOLD + "GUILD CHAT.");
            player.sendMessage(ChatColor.GRAY + "Use /gl (or tab) to speak in global.");
            return true;
        }


        String guildName = wrapper.getName();
//        String guildName = GuildDatabaseAPI.get().getGuildOf(player.getUniqueId());

        if (args.length == 0) {
            GuildMechanics.getInstance().toggleGuildChat(player);
            return true;
        }

        StringBuilder msg = new StringBuilder(args[0]);
        for (int arg = 1; arg < args.length; arg++) msg.append(" ").append(args[arg]);

        wrapper.sendGuildMessage(msg.toString(), false);
//        GuildMechanics.getInstance().sendMessageToGuild(guildName, msg.toString());
        return false;
    }
}
