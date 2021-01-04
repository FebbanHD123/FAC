package de.febanhd.anticrash.checks.impl;

import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.config.ConfigCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveCheck extends AbstractCheck implements Listener {

    private long lastFlagClear = System.currentTimeMillis();

    public MoveCheck() {
        super("MoveCheck");
    }

    /*
    * This check is from Illigalspigot
    */

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        double distance = event.getFrom().distance(event.getTo());
        int maxDistance = ConfigCache.getInstance().getValue("moveCheck.flagDistance", 5, Integer.class);
        if(distance > maxDistance) {
            this.sendCrashWarning(event.getPlayer(), "Player moved to fast (> " + maxDistance + ")");
        }
    }
}
