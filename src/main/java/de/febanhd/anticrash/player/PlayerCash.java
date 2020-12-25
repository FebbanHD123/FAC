package de.febanhd.anticrash.player;

import com.google.common.collect.Lists;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerCash {

    private ArrayList<FACPlayer> players = Lists.newArrayList();

    public void register(Player bukkitPlayer) {
        FACPlayer player = new FACPlayer(System.currentTimeMillis(), bukkitPlayer);
        this.players.add(player);
    }

    public void remove(UUID uuid) {
        FACPlayer player = this.getPlayer(uuid);
        if(player != null) {
            player.unregister();
            this.players.remove(player);
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

    public void unregisterAll() {
        this.players.forEach(FACPlayer::unregister);
    }
}
