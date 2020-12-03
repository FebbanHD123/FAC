package de.febanhd.anticrash.checks.impl;

import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.febanhd.anticrash.AntiCrash;
import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.config.ConfigCach;
import de.febanhd.anticrash.nettyinjections.MCChannelInjection;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DosCheck extends AbstractCheck {

    private CopyOnWriteArrayList<Connection> connections = Lists.newCopyOnWriteArrayList();
    private HashMap<String, Long> blockedIPs = Maps.newHashMap();
    private LinkedHashMap<String, Long> lastConnected = Maps.newLinkedHashMap();

    private MCChannelInjection injection;

    public boolean isAttack;
    private long attackStartedAt;
    private int cpsToUnlock;

    private int connectionPerSecond = 0;
    private int cpsLimit = ConfigCach.getInstance().getValue("doscheck.cpsLimit", 130, Integer.class);
    private long blockTime = ConfigCach.getInstance().getValue("doscheck.blocktime", 300000, Integer.class);

    private boolean debug = ConfigCach.getInstance().getValue("debugMode", false, Boolean.class);
    public long blockedConnections = 0;

    private String debugActionbarLayout, attackActionbarLayout;

    public DosCheck() {
        super("DosCheck");
        if(!this.isEnable()) return;
        try {
            this.injection = new MCChannelInjection(this);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        this.isAttack = false;

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this.getPlugin(), () -> {
            this.connectionPerSecond = this.getConnections(1000).size();
            if (!this.connections.isEmpty()) {
                List<Connection> connectionsToRemove = Lists.newArrayList();
                List<Connection> aliveConnections = this.getConnections(30);
                this.connections.forEach(connection -> {
                    if (!aliveConnections.contains(connection)) {
                        connectionsToRemove.add(connection);
                    }
                });
                connectionsToRemove.forEach(this.connections::remove);
            }

            if (this.isAttack && this.connectionPerSecond <= this.cpsToUnlock && this.attackStartedAt + 10000 < System.currentTimeMillis()) {
                this.endAttackMode();
            }

            if (!blockedIPs.isEmpty() && !this.isAttack) {
                ArrayList<String> hostsToUnblock = Lists.newArrayList();

                this.blockedIPs.forEach((host, blockedAT) -> {
                    if (blockedAT + blockTime < System.currentTimeMillis()) {
                        hostsToUnblock.add(host);
                    }
                });
                hostsToUnblock.forEach(host -> {
                    this.blockedIPs.remove(host);
                });
            }

            if (!this.isAttack && this.connectionPerSecond > this.cpsLimit) {
                this.enableAttackMode(this.cpsLimit);
            }
            String defaultDebugActionBarLayout = "&fCPS &7| &c%cps% &7| &fPing &c%ping% &7| &fBlocked-IPs &c%ips%";
            String defaultActionBarLayout = "&cAttack &f| &fCPS &7| &c%cps% &7| &fPing &c%ping% &7| &fBlocked-Connections &c%blocked% &7| &fBlocked-IPs &c%ips%";
            this.debugActionbarLayout = ChatColor.translateAlternateColorCodes('&',
                    ConfigCach.getInstance().getValue("doscheck.actionbarlayout.debug", defaultDebugActionBarLayout, String.class)
                    .replaceAll("%cps%", String.valueOf(this.connectionPerSecond))
                    .replaceAll("%blocked%", String.valueOf(this.blockedConnections))
                    .replaceAll("%ips%", String.valueOf(this.blockedIPs.size())));
            this.attackActionbarLayout = ChatColor.translateAlternateColorCodes('&',
                    ConfigCach.getInstance().getValue("doscheck.actionbarlayout.attack", defaultActionBarLayout, String.class)
                    .replaceAll("%cps%", String.valueOf(this.connectionPerSecond))
                    .replaceAll("%blocked%", String.valueOf(this.blockedConnections))
                    .replaceAll("%ips%", String.valueOf(this.blockedIPs.size())));

            if (this.debug && !this.isAttack) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if(player.hasPermission("anticrash.notify")) {
                        String message = this.debugActionbarLayout.replaceAll("%ping%", ((CraftPlayer) player).getHandle().ping + "ms");
                        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte) 2);
                        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                    }
                });
            }

            if (this.isAttack) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if(player.hasPermission("anticrash.notify")) {
                        String message = this.attackActionbarLayout.replaceAll("%ping%", ((CraftPlayer) player).getHandle().ping + "ms");
                        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte) 2);
                        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                    }
                });
            }
        }, 0, 10);
    }

    public boolean initChannel(ChannelHandlerContext channelContext) throws Exception {
        Connection connection = new Connection(channelContext);
        String host = connection.getHost();
        this.connections.add(connection);
        this.lastConnected.put(host, System.currentTimeMillis());
        if (this.blockedIPs.containsKey(host)) {
            return false;
        }

        if(!this.canConnect(host)) return false;

        if (!this.isAttack && this.connectionPerSecond > this.cpsLimit) {
            this.enableAttackMode(this.cpsLimit);
            return false;
        }

        if (!this.blockedIPs.containsKey(host) && this.getConnections(host, 1000).size() > ConfigCach.getInstance().getValue("doscheck.cps_limit_ip", 20, Integer.class)) {
            this.blockedIPs.put(host, System.currentTimeMillis());
            this.enableAttackMode(ConfigCach.getInstance().getValue("doscheck.cps_limit_ip", 20, Integer.class));
            return false;
        }
        return true;
    }

    public boolean handleObject(ChannelHandlerContext ctx, Object o) {
            Connection connection = null;
            try {
                connection = new Connection(ctx);
            } catch (Exception e) {
                return true;
            }
            String host = connection.getHost();
            if(this.blockedIPs.containsKey(host)) {
               return false;
            }

            if(this.isPlayerChannel(ctx.channel())) return true;

            if (o instanceof UnpooledUnsafeDirectByteBuf) {
                ByteBuf byteBuf = Unpooled.wrappedBuffer((UnpooledUnsafeDirectByteBuf) o);
                int capacity = byteBuf.capacity();
                if (capacity > ConfigCach.getInstance().getValue("doscheck.maxDataCapacity", 1000, Integer.class)) {
                    this.blockIP(host, "Too big datas (> " + ConfigCach.getInstance().getValue("doscheck.maxDataCapacity", 1000, Integer.class) + ")");
                    return false;
                }
            }
            return true;
    }

    private boolean isPlayerChannel(Channel channel) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(((CraftPlayer)player).getHandle().playerConnection.networkManager.channel.equals(channel)) {
                return true;
            }
        }
        return false;
    }

    public boolean canConnect(String host) {

        if (blockedIPs.containsKey(host)) return false;

        if (this.getConnections(host, 60000).size() > 150) {
            this.blockIP(host, "Too many connections in the last minute (> 150)");
        }

        return !this.blockedIPs.containsKey(host);
    }

    public List<Connection> getConnections(String host, long time) {
        List<Connection> connections = Lists.newArrayList();
        this.connections.forEach(connection -> {
            if (connection.getHost().equals(host) && connection.getConnectedAt() + time > System.currentTimeMillis()) {
                connections.add(connection);
            }
        });
        return connections;
    }

    public boolean isBlocked(Channel channel) {
        try {
            String host = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
            return this.blockedIPs.containsKey(host);
        }catch (Exception e) {
            return false;
        }
    }

    private long lastBlockMessageSend = 0;

    public void blockIP(String ip, String reason) {
        this.blockedIPs.put(ip, System.currentTimeMillis());
        if(!this.isAttack && (lastBlockMessageSend + 10000 < System.currentTimeMillis())) {
            lastBlockMessageSend = System.currentTimeMillis();
            Bukkit.getScheduler().scheduleSyncDelayedTask(AntiCrashPlugin.getPlugin(), () -> {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if(player.hasPermission("anticrash.notify")) {
                        player.sendMessage(AntiCrash.PREFIX + "§cBlocked IP: §7" + ip);
                        player.sendMessage(AntiCrash.PREFIX + "§cReason: §7" + reason);
                    }
                });
                Bukkit.getConsoleSender().sendMessage(AntiCrash.PREFIX + "§cBlocked IP: §7" + ip);
                Bukkit.getConsoleSender().sendMessage(AntiCrash.PREFIX + "§cReason: §7" + reason);
            });
        }else if(!this.isAttack && !this.debug) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if(player.hasPermission("anticrash.notify")) {
                    PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(AntiCrash.PREFIX + "§cBlocked IP: §7" + ip), (byte) 2);
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                }
            });
        }
    }

    public List<Connection> getConnections(long time) {
        List<Connection> connections = Lists.newArrayList();
        this.connections.forEach(connection -> {
            if (connection.getConnectedAt() + time > System.currentTimeMillis()) {
                connections.add(connection);
            }
        });
        return connections;
    }

    public CopyOnWriteArrayList<Connection> getConnectionList() {
        return this.connections;
    }

    public void enableAttackMode(int cpsToUnlock) {
        if(this.isAttack) return;
        this.isAttack = true;
        this.cpsToUnlock = cpsToUnlock;
        this.blockedConnections = 0;
        this.attackStartedAt = System.currentTimeMillis();
        new Broadcast("anticrash.notify", receiver -> receiver.sendMessage(AntiCrash.PREFIX + "§cThe server is under attack! Try to block bad ips..."));
    }

    public void endAttackMode() {
        this.isAttack = false;
        new Broadcast("anticrash.notify", receiver -> receiver.sendMessage(AntiCrash.PREFIX + "§aThe attack is over! Currently " + this.blockedIPs.size() + " IP addresses are blocked."));
    }

    public void unblockAllHosts() {
        this.blockedIPs.clear();
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {

    }

    public MCChannelInjection getInjection() {
        return injection;
    }

    public static class Connection {

        private Channel channel;
        private ChannelHandlerContext channelContext;
        private String host;
        private int port;
        private final long connectedAt;

        public Connection(ChannelHandlerContext channelContext) {
            this.channelContext = channelContext;
            this.connectedAt = System.currentTimeMillis();
            if(channelContext == null) return;
            this.host = ((InetSocketAddress) channelContext.channel().remoteAddress()).getAddress().getHostAddress();
            if (host == null) {
                return;
            }
            this.port = ((InetSocketAddress) channelContext.channel().remoteAddress()).getPort();
        }

        public Connection(Channel channel) {
            this.channel = channel;
            this.connectedAt = System.currentTimeMillis();
            this.host = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
            this.port = ((InetSocketAddress) channel.remoteAddress()).getPort();
        }

        public int getPort() {
            return port;
        }

        public String getHost() {
            return host;
        }

        public long getConnectedAt() {
            return connectedAt;
        }
    }
}
