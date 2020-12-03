package de.febanhd.anticrash.plugin;

import de.febanhd.anticrash.AntiCrash;
import de.febanhd.anticrash.checks.impl.DosCheck;
import de.febanhd.anticrash.commands.AntiCrashCommand;
import de.febanhd.anticrash.commands.UnblockIPsCommand;
import de.febanhd.anticrash.listener.PlayerConnectionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiCrashPlugin extends JavaPlugin {

    private static AntiCrashPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;

        if(Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            Bukkit.getConsoleSender().sendMessage(AntiCrash.PREFIX + "§cThe plugin requires ProtocolLib!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.getCommand("unblockips").setExecutor(new UnblockIPsCommand());
        this.getCommand("de/febanhd/anticrash").setExecutor(new AntiCrashCommand());
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(), this);
        AntiCrash.init(this);

        Bukkit.getConsoleSender().sendMessage(AntiCrash.PREFIX + "§aProtocolLib was detected! The plugin is now ready!");
        Bukkit.getConsoleSender().sendMessage(AntiCrash.PREFIX + "§eAntiCrash by §bFebanHD - §7Thanks for use :D");
    }

    @Override
    public void onDisable() {
        DosCheck dosCheck = (DosCheck)AntiCrash.getInstance().getCheck(DosCheck.class);
        if(dosCheck != null) {
            dosCheck.getInjection().close();
            this.getLogger().info("Uninjected MC-Channels");
        }
        AntiCrash.getInstance().getPlayerCash().unregisterAll();
    }

    public static AntiCrashPlugin getPlugin() {
        return plugin;
    }
}
