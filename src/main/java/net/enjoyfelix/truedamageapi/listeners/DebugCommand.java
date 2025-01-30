package net.enjoyfelix.truedamageapi.listeners;

import net.enjoyfelix.truedamageapi.DamageAPI;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player))
            return false;

        if (args.length != 4){
            sender.sendMessage("§4usage: /debug <player> <effect> <amplifier> <time in ticks>");
            return false;
        }

        final Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage("§4Could not find Player'" + args[0] + "'");
            return false;
        }

        final double amplifier;
        try {
            amplifier = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§4'" + args[2] + "' is not a number !");
            return false;
        }

        final int time;
        try {
            time = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§4'" + args[3] + "' is not a number !");
            return false;
        }

        switch (args[1].toLowerCase()) {
            case "strength": {
                DamageAPI.getInstance().getStrengthProvider().setAmplier(player, amplifier, time, true);
                sender.sendMessage(player.getDisplayName() + "§r now has " + DamageAPI.getInstance().getStrengthProvider().getTotalAsPercent(player) + "% of strength");
                break;
            }

            default: {
                sender.sendMessage("§4 Effect can only be strength/weakness/resistance, not '" + args[1] + "' !");
                return false;
            }
        }
        return true;
    }
}
