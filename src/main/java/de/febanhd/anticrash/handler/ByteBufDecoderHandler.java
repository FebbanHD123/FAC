package de.febanhd.anticrash.handler;

import de.febanhd.anticrash.AntiCrash;
import de.febanhd.anticrash.config.ConfigCach;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class ByteBufDecoderHandler extends ByteToMessageDecoder {

    private final Player player;
    private long lastWarningSended = 0;

    public ByteBufDecoderHandler(Player player) {
        this.player = player;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        if(!player.isOnline()) return;

        int maxCapacity = ConfigCach.getInstance().getValue("decode.maxCapacity", 16500, Integer.class);

        if(byteBuf.capacity() < 0) {
            this.sendCrashWarning(player, "Too low packet capacity! (< 0)");
            return;
        }

        if(byteBuf.refCnt() < 1) {
            this.sendCrashWarning(player, "Too low packet refCnt! (< 0)");
            return;
        }

        if(byteBuf.capacity() > maxCapacity) {
            this.sendCrashWarning(player, "Too high packet capacity (> " + maxCapacity + ")");
            return;
        }

        list.add(byteBuf.readBytes(byteBuf.readableBytes()));
    }

    public void sendCrashWarning(Player player, String reason) {
        ((CraftPlayer)player).getHandle().playerConnection.networkManager.channel.close();
        if(this.lastWarningSended + 1500 > System.currentTimeMillis()) return;
        this.lastWarningSended = System.currentTimeMillis();
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
        Bukkit.getScheduler().scheduleSyncDelayedTask(AntiCrashPlugin.getPlugin(), () -> {
            Bukkit.getOnlinePlayers().forEach(players -> {
                players.sendMessage(AntiCrash.PREFIX + "§c" + finalPlayerName + " §7tried to Crash the Server!");
                players.sendMessage(AntiCrash.PREFIX + "§cReason: §7" + reason);
            });
            Bukkit.getConsoleSender().sendMessage(AntiCrash.PREFIX + "§c" + finalPlayerName + " §7tried to Crash the Server!");
            Bukkit.getConsoleSender().sendMessage(AntiCrash.PREFIX + "§cReason: §7" + reason);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        throwable.printStackTrace();
    }
}
