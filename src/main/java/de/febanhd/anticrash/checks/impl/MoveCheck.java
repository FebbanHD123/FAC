package de.febanhd.anticrash.checks.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.febanhd.anticrash.AntiCrash;
import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.config.ConfigCache;
import de.febanhd.anticrash.player.FACPlayer;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MoveCheck extends AbstractCheck implements Listener {

    private HashMap<UUID, MoveObject> playerLocationsLastTick = Maps.newHashMap();
    private HashMap<UUID, Integer> flags = Maps.newHashMap();
    private ArrayList<UUID> teleports = Lists.newArrayList();

    private long lastFlagClear = System.currentTimeMillis();

    public MoveCheck() {
        super("MoveCheck", PacketType.Play.Client.POSITION);
        if(!this.isEnable()) return;
        Bukkit.getPluginManager().registerEvents(this, this.getPlugin());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(AntiCrashPlugin.getPlugin(), () -> {
            Bukkit.getOnlinePlayers().forEach(player -> playerLocationsLastTick.put(player.getUniqueId(), new MoveObject(player.getLocation())));
            if(lastFlagClear + 60000 < System.currentTimeMillis()) {
                this.flags.clear();
            }
        }, 0, 1);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if(!this.isOnline(event.getPlayer())) return;
        if(AntiCrash.getInstance().getTpsCalculator().getCurrentTps() < ConfigCache.getInstance().getValue("movecheck.maxTps", 15, Integer.class)) return;
        FACPlayer facPlayer = AntiCrash.getInstance().getPlayerCash().getPlayer(event.getPlayer().getUniqueId());
        if(facPlayer == null || facPlayer.getJoinedAt() + 2500 > System.currentTimeMillis()) return; //check if hes joining
        if(!this.playerLocationsLastTick.containsKey(event.getPlayer().getUniqueId())) return;
        PacketContainer packet = event.getPacket();
        StructureModifier<Double> doubles = packet.getDoubles();
        Player player = event.getPlayer();
        double x = doubles.readSafely(0);
        double y = doubles.readSafely(1);
        double z = doubles.readSafely(2);
        Location location = new Location(event.getPlayer().getWorld(), x, y, z);

        MoveObject lastMove = this.playerLocationsLastTick.get(player.getUniqueId());

        Location lastLocation = lastMove.location;
        double distance = lastLocation.distance(location);

        if(distance < ConfigCache.getInstance().getValue("movecheck.maxDistance", 75, Integer.class)) return;

        if(this.teleports.contains(player.getUniqueId())) {
            this.teleports.remove(player.getUniqueId());
            return;
        }

        Bukkit.getScheduler().runTaskLater(this.getPlugin(), () -> {
            if(this.isOnline(player)) {
                this.check(player, event, distance, lastMove);
            }
        }, 2);
    }

    private void check(Player player, PacketEvent event, double distance, MoveObject lastMove) {
        if(lastMove.movedAt + 100 + 55 < System.currentTimeMillis()) return;
        PacketContainer packet = event.getPacket();
        UUID uuid = player.getUniqueId();
        if (this.teleports.contains(uuid)) {
            this.teleports.remove(uuid);
            return;
        }
        if (distance >= ConfigCache.getInstance().getValue("movecheck.maxDistance", 75, Integer.class)) {
            event.setCancelled(true);
            if (this.flags.containsKey(player.getUniqueId())) {
                this.flags.put(player.getUniqueId(), this.flags.get(player.getUniqueId()) + 1);
            } else {
                this.flags.put(player.getUniqueId(), 1);
            }
        }
        if (this.flags.containsKey(player.getUniqueId()) && this.flags.get(player.getUniqueId()) > ConfigCache.getInstance().getValue("movecheck.max_flags", 5, Integer.class)) {
            this.sendCrashWarning(player, event, "Moved too often too fast.");
            this.flags.remove(player.getUniqueId());

        }
    }

    private boolean isOnline(Player player) {
        return ((CraftPlayer)player).getHandle().playerConnection.networkManager.channel.isOpen();
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        this.teleports.add(event.getPlayer().getUniqueId());
    }

    private class MoveObject {
        private final Location location;
        private final long movedAt;

        public MoveObject(Location location) {
            this.location = location;
            this.movedAt = System.currentTimeMillis();
        }
    }
}
