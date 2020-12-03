package de.febanhd.anticrash.checks.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.febanhd.anticrash.AntiCrash;
import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.config.ConfigCach;
import de.febanhd.anticrash.nettyinjections.PacketInjection;
import de.febanhd.anticrash.player.FACPlayer;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PacketSpamCheck extends AbstractCheck implements PacketInjection.PacketListener {

    private static final List<Class<?>> IGNORE_PACKET_CLASSES = Lists.newArrayList(Arrays.asList(PacketPlayInBlockDig.class, PacketPlayInUseEntity.class, PacketPlayInEntityAction.class, PacketPlayInFlying.class, PacketPlayInFlying.PacketPlayInPositionLook.class));

    private HashMap<UUID, PacketSpamProfile> profileHashMap = Maps.newHashMap();

    public PacketSpamCheck() {
        super("PacketSpamCheck");
    }

    @Override
    public boolean handlePacket(Player player, Packet<?> packet) {
        if(PacketSpamCheck.IGNORE_PACKET_CLASSES.contains(packet.getClass())) return true;
        FACPlayer facPlayer = AntiCrash.getInstance().getPlayerCash().getPlayer(player.getUniqueId());
        if(facPlayer == null || facPlayer.getJoinedAt() + 2500 > System.currentTimeMillis()) return true; //check if hes joining
        PacketSpamProfile profile = profileHashMap.get(player.getUniqueId());
        if(profile == null) {
            profileHashMap.put(player.getUniqueId(), profile = new PacketSpamProfile(player));
            profile.setLastPacket(packet);
            return true;
        }else {
            long delay = getDelay(packet);
            if(profile.getLastPacketSended(packet) + delay > System.currentTimeMillis()) {
                this.sendFlagWarning(player, packet);
                return false;
            }
        }
        profile.setLastPacket(packet);
        return true;
    }

    private long getDelay(Packet<?> packet) {
        String packetName = packet.getClass().getSimpleName();
        if(ConfigCach.getInstance().contains("spamcheck.customDelay." + packetName)) {
            return ConfigCach.getInstance().getValue("spamcheck.customDelay." + packetName, 20, Integer.class);
        }
        return ConfigCach.getInstance().getValue("spamcheck.defaultDelay", 20, Integer.class);
    }

    @Override
    public void registerFACPlayer(FACPlayer player) {
        player.getPacketInjection().addPacketListener(this);
    }

    public void sendFlagWarning(Player player, Packet<?> packet) {
        String playerName = player.getName();
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.getPlugin(), () -> {
            new Broadcast("anticrash.notify", players -> {
                players.sendMessage(AntiCrash.PREFIX + "§c" + playerName + " §7send a packet too fast!");
                players.sendMessage(AntiCrash.PREFIX + "§cPacket: §7" + packet.getClass().getSimpleName());
            });
        });
    }

    private class PacketSpamProfile {

        private Player player;
        private HashMap<Class<?>, Long> lastPacketSended = Maps.newHashMap();

        public PacketSpamProfile(Player player) {
            this.player = player;
        }

        public void setLastPacket(Packet<?> lastPacket) {
            this.lastPacketSended.put(lastPacket.getClass(), System.currentTimeMillis());
        }

        public long getLastPacketSended(Packet<?> packet) {
            if(this.lastPacketSended.containsKey(packet.getClass())) return this.lastPacketSended.get(packet.getClass());
            else {
                return 0;
            }
        }
    }
}
