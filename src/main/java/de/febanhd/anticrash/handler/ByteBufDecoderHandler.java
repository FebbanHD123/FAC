package de.febanhd.anticrash.handler;

import de.febanhd.anticrash.AntiCrash;
import de.febanhd.anticrash.config.ConfigCache;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import de.febanhd.anticrash.utils.NMSUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ByteBufDecoderHandler extends ByteToMessageDecoder {

    private final Player player;
    private long lastWarningSended = 0;

    private int byteBufFrequency = 0;
    private long lastByteBufFrequency = 0;

    public ByteBufDecoderHandler(Player player) {
        this.player = player;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {

        if(byteBuf.capacity() < 0) {
            this.sendCrashWarning(player, "Too low packet capacity! (< 0)");
            return;
        }

        if(byteBuf.refCnt() < 1) {
            this.sendCrashWarning(player, "Too low packet refCnt! (< 0)");
            return;
        }

        if (byteBuf.array().length > 1000) {
            if (lastByteBufFrequency == 0)
                lastByteBufFrequency = System.currentTimeMillis();
            if (System.currentTimeMillis() - lastByteBufFrequency >= 1000L) {
                lastByteBufFrequency = System.currentTimeMillis();
                byteBufFrequency = 0;
            } else {
                byteBufFrequency++;
            }
            if (byteBufFrequency > 5) {
                this.sendCrashWarning(player, "Too high bytebuf frequency");
                return;
            }
        }
        if (byteBuf.array().length > ConfigCache.getInstance().getValue("decode.maxLength", 8000, Integer.class)) {
            this.sendCrashWarning(player, "Too high packet size");
            return;
        }


        if(!player.isOnline()) {
            this.sendCrashWarning(player, "Player is offline");
            return;
        }

        Packet<?> packet = null;
        if (byteBuf.readableBytes() != 0) {
            PacketDataSerializer packetDataSerializer = new PacketDataSerializer(byteBuf.copy()); //Copy bytebuf
            int packetID = packetDataSerializer.e();
            packet = ctx.channel().attr(NetworkManager.c).get().a(EnumProtocolDirection.SERVERBOUND, packetID); //get packet by packet id
        }
        int maxCapacity = ConfigCache.getInstance().getValue("decode.maxCapacity", 16500, Integer.class);

        if(packet != null) {
            String customCapacityConfigPath = "decode.customCapacity." + packet.getClass().getSimpleName();
            if (ConfigCache.getInstance().contains(customCapacityConfigPath)) {
                Object customCapacity = ConfigCache.getInstance().getValue(customCapacityConfigPath);
                if (customCapacity instanceof Integer) {
                    maxCapacity = (Integer) customCapacity;
                }
            }
        }

        if(byteBuf.capacity() > maxCapacity) {
            this.sendCrashWarning(player, "Too high packet capacity (> " + maxCapacity + ")");
            return;
        }

        list.add(byteBuf.readBytes(byteBuf.readableBytes()));
    }

    public void sendCrashWarning(Player player, String reason) {
        try {
            if (this.lastWarningSended + 1500 > System.currentTimeMillis()) return;
            this.lastWarningSended = System.currentTimeMillis();
            String playerName;
            if (player != null && player.getName() != null) {
                playerName = player.getName();
                if (Bukkit.getPlayer(playerName) == null) {
                    playerName = "§oUnknown Name";
                }
            } else {
                playerName = "§oUnknown Name";
            }
            final String finalPlayerName = playerName;
            Bukkit.getScheduler().scheduleSyncDelayedTask(AntiCrashPlugin.getPlugin(), () -> {
                player.kickPlayer("&cCrashing");
                Bukkit.getOnlinePlayers().forEach(players -> {
                    players.sendMessage(AntiCrash.PREFIX + "§c" + finalPlayerName + " §7tried to Crash the Server!");
                    players.sendMessage(AntiCrash.PREFIX + "§cReason: §7" + reason);
                });
                Bukkit.getConsoleSender().sendMessage(AntiCrash.PREFIX + "§c" + finalPlayerName + " §7tried to Crash the Server!");
                Bukkit.getConsoleSender().sendMessage(AntiCrash.PREFIX + "§cReason: §7" + reason);
            });
            NMSUtils.getChannel(player).close();
        }catch (Exception e) {}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        throwable.printStackTrace();
    }
}
