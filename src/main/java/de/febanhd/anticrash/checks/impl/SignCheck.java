package de.febanhd.anticrash.checks.impl;

import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.config.ConfigCache;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignCheck extends AbstractCheck implements Listener {
    public SignCheck() {
        super("SignCheck");
        if(this.isEnable())
            Bukkit.getPluginManager().registerEvents(this, AntiCrashPlugin.getPlugin());
    }

    @EventHandler
    public void onSignUpdate(SignChangeEvent event) {
        boolean badSign = false;
        int maxLength = ConfigCache.getInstance().getValue("signcheck.maxLength", 50, Integer.class);
        for(String line : event.getLines()) {
            int lineLength = line.length();
            if(lineLength > maxLength) {
                badSign = true;
                break;
            }
        }
        if(badSign) {
            this.sendCrashWarning(event.getPlayer(), "Placed a sign with too many characters (> " + maxLength + ")");
            event.setCancelled(true);
        }
    }

}
