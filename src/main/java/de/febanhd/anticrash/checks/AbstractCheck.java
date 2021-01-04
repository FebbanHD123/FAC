package de.febanhd.anticrash.checks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.Maps;
import de.febanhd.anticrash.AntiCrash;
import de.febanhd.anticrash.config.ConfigCache;
import de.febanhd.anticrash.player.FACPlayer;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import de.febanhd.anticrash.utils.NMSUtils;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Set;

public class AbstractCheck extends PacketAdapter implements ICheck {

    private static HashMap<Player, String> lastReasonsMap = Maps.newHashMap();
    private static HashMap<Player, Long> lastMessage = Maps.newHashMap();

    private String name;

    public AbstractCheck(final String name) {
        super(AntiCrashPlugin.getPlugin(), PacketType.Play.Client.LOOK);
        this.name = name;
    }

    public AbstractCheck(final String name, final PacketType... types) {
        super(AntiCrashPlugin.getPlugin(), types);
        this.name = name;
        if(this.isEnable())
            ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    public AbstractCheck(final String name, final Set<PacketType> packetTypes) {
        super(AntiCrashPlugin.getPlugin(), packetTypes);
        this.name = name;
        if(this.isEnable())
            ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    public boolean isEnable() {
        return ConfigCache.getInstance().isCheckEnable(this.name);
    }

    public void sendCrashWarning(Player player, PacketEvent event, String reason) {
        event.setCancelled(true);
        PacketContainer packet = event.getPacket();
        if(lastReasonsMap.containsKey(player) && lastReasonsMap.get(player).equals(reason)) return;
        lastReasonsMap.put(player, reason);

        String playerName = this.getPlayerName(player);

        if(!lastMessage.containsKey(player)) {
            lastMessage.put(player, System.currentTimeMillis() - 100);
        }
        if(lastMessage.get(player) + 10 <= System.currentTimeMillis()) {
            lastMessage.put(player, System.currentTimeMillis());
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.getPlugin(), () -> {
                player.kickPlayer("§cCrashing");
                new Broadcast("anticrash.notify", players -> {
                    players.sendMessage(AntiCrash.PREFIX + "§c" + playerName + " §7tried to Crash the Server!");
                    players.sendMessage(AntiCrash.PREFIX + "§cReason: §7" + reason);
                    players.sendMessage(AntiCrash.PREFIX + "§cPacket: §7" + packet.getType().getPacketClass().getSimpleName());
                });
            });
        }
        this.closeChannel(player);
    }

    public void sendInvalidPacketWarning(Player player, PacketEvent event, String reason) {
        event.setCancelled(true);
        if(lastReasonsMap.containsKey(player) && lastReasonsMap.get(player).equals(reason)) return;
        lastReasonsMap.put(player, reason);

        String playerName = this.getPlayerName(player);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.getPlugin(), () -> {
            new Broadcast("anticrash.notify", players -> {
              players.sendMessage(AntiCrash.PREFIX + "§c" + playerName + " §7send an invalid packet!");
              players.sendMessage(AntiCrash.PREFIX + "§cReason: §7" + reason);
            });
        });
    }

    public void sendCrashWarning(Player player, String reason) {
        this.closeChannel(player);
        if(lastReasonsMap.containsKey(player) && lastReasonsMap.get(player).equals(reason)) return;
        lastReasonsMap.put(player, reason);

        String playerName = this.getPlayerName(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.getPlugin(), () -> {
            new Broadcast("anticrash.notify", players -> {
                players.sendMessage(AntiCrash.PREFIX + "§c" + playerName + " §7tried to Crash the Server!");
                players.sendMessage(AntiCrash.PREFIX + "§cReason: §7" + reason);
            });
        });
    }

    @Override
    public void registerFACPlayer(FACPlayer player) {

    }

    public void closeChannel(Player player) {
        try {
            Channel channel = NMSUtils.getChannel(player);
            if (channel.isOpen()) {
                channel.close();
            }
        }catch (Exception e) {}
    }

    public void safeKick(Player player, String reason) {
        ((CraftPlayer)player).getHandle().playerConnection.disconnect(reason);
    }

    private String getPlayerName(Player player) {
        String playerName;
        if(player != null && player.getName() != null) {
            playerName = player.getName();
            if(Bukkit.getPlayer(playerName) == null) {
                playerName = "§oUnknown Name";
            }
        }else {
            playerName = "§oUnknown Name";
        }
        final String finalPlayerName = playerName;
        return finalPlayerName;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static class Broadcast {

        private String permission;
        private final BroadcastCallable callable;

        public Broadcast(String permission, BroadcastCallable callable) {
            this.permission = permission;
            this.callable = callable;
            this.send();
        }

        public Broadcast(BroadcastCallable callable) {
            this.permission = Strings.EMPTY;
            this.callable = callable;
            this.send();
        }

        private void send() {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if(permission.isEmpty()) {
                    callable.send(player);
                }else if(player.hasPermission(permission)) {
                    callable.send(player);
                }
            });
            callable.send(Bukkit.getConsoleSender());
        }

        public interface BroadcastCallable {
            void send(CommandSender receiver);
        }
    }
}
