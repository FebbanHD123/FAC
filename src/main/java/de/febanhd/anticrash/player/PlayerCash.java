package de.febanhd.anticrash.player;

import com.google.common.collect.Lists;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerCash {

    public PlayerCash() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(AntiCrashPlugin.getPlugin(), () -> {
            List<FACPlayer> playersToRemove = Lists.newArrayList();
            this.players.forEach(player -> {
                player.setLastOnline(System.currentTimeMillis());
                if(player.getLastOnlineAt() + 60000 < System.currentTimeMillis()) {
                    playersToRemove.add(player);
                }
            });
            this.players.removeAll(playersToRemove);
        }, 0, 1);
    }

    private ArrayList<FACPlayer> players = Lists.newArrayList();

    public void register(Player bukkitPlayer) {
        FACPlayer player = getPlayer(bukkitPlayer.getUniqueId()) == null ? new FACPlayer(System.currentTimeMillis(), bukkitPlayer) : getPlayer(bukkitPlayer.getUniqueId());
        player.setOnline();
        this.players.add(player);
    }

    public void handleQuit(UUID uuid) {
        FACPlayer player = this.getPlayer(uuid);
        if(player != null) {
            player.unregister();
            player.setLastOnline(System.currentTimeMillis());
        }
    }

    public FACPlayer getPlayer(UUID uuid) {
        for(FACPlayer player : this.players) {
            if(player.getUuid().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    public FACPlayer getPlayerByIP(String ip) {
        for (FACPlayer player : this.players) {
            if(player.getIp().equals(ip)) {
                return player;
            }
        }
        return null;
    }

    public void unregisterAll() {
        this.players.forEach(FACPlayer::unregister);
    }
}
