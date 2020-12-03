package de.febanhd.anticrash.nettyinjections;

import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PacketInjection extends MessageToMessageDecoder<Packet<?>> {

    private final Player player;
    private ArrayList<PacketListener> packetListeners = Lists.newArrayList();

    public PacketInjection(Player player) {
        this.player = player;
    }

    public void addPacketListener(PacketListener listeners) {
        this.packetListeners.add(listeners);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
        boolean handle = true;
        try {
            for (PacketListener packetListener : this.packetListeners) {
                if(!packetListener.handlePacket(this.player, packet)) handle = false;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        if(handle)
            list.add(packet);
    }

    public void inject() {
        CraftPlayer player = (CraftPlayer)this.player;
        player.getHandle().playerConnection.networkManager.channel.pipeline().addAfter("decoder", "ac_packet_decoder", this);
    }

    public void remove() {
        CraftPlayer player = (CraftPlayer)this.player;
        Channel channel = player.getHandle().playerConnection.networkManager.channel;
        if(channel.pipeline().get("ac_packet_decoder") != null)
            channel.pipeline().remove("ac_packet_decoder");
    }

    public interface PacketListener {
        boolean handlePacket(Player player, Packet<?> packet);
    }
}
