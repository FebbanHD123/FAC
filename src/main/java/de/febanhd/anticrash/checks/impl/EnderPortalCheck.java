package de.febanhd.anticrash.checks.impl;

import com.comphenix.protocol.events.PacketEvent;
import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EnderPortalCheck extends AbstractCheck implements Listener {

    public EnderPortalCheck() {
        super("EnderPortalCrashCheck");
        if(this.isEnable())
            Bukkit.getPluginManager().registerEvents(this, AntiCrashPlugin.getPlugin());
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        //Do nothing :D
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if(event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(this.getPlugin(), () -> event.getPlayer().teleport(event.getTo()), 5);
        }
    }

}
