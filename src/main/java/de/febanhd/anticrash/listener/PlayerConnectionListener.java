package de.febanhd.anticrash.listener;

import de.febanhd.anticrash.AntiCrash;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        AntiCrash.getInstance().getPlayerCash().register(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        AntiCrash.getInstance().getPlayerCash().handleQuit(event.getPlayer().getUniqueId());
    }
}
