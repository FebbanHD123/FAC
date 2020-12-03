package de.febanhd.anticrash.commands;

import de.febanhd.anticrash.AntiCrash;
import de.febanhd.anticrash.checks.impl.DosCheck;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UnblockIPsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender.hasPermission("anticrash.unblockip")) {
            DosCheck dosCheck = (DosCheck) AntiCrash.getInstance().getCheck(DosCheck.class);
            dosCheck.unblockAllHosts();
            sender.sendMessage(AntiCrash.PREFIX + "§7All ip addresses were §cunblocked§7.");
        }

        return false;
    }
}
