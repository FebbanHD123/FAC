package de.febanhd.anticrash.commands;

import de.febanhd.anticrash.AntiCrash;
import de.febanhd.anticrash.checks.impl.nbt.NBTTagCheck;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AntiCrashCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        sender.sendMessage(AntiCrash.PREFIX + "§cFAC by §eFebanHD §8| AntiCrash & AntiDos");
        sender.sendMessage(AntiCrash.PREFIX + "§cVersion: §7" + AntiCrashPlugin.getPlugin().getDescription().getVersion());
        sender.sendMessage(AntiCrash.PREFIX + "§cChecks: §7" + AntiCrash.getInstance().getChecks().size());
        sender.sendMessage(AntiCrash.PREFIX + "§cNBTChecks: §7" + ((NBTTagCheck)AntiCrash.getInstance().getCheck(NBTTagCheck.class)).getChecks().size());

        return false;
    }
}
