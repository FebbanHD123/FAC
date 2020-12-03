package de.febanhd.anticrash.nettyinjections;

import de.febanhd.anticrash.handler.ByteBufDecoderHandler;
import io.netty.channel.Channel;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NettyDecodeInjection {

    private Channel channel;
    private Player player;

    public NettyDecodeInjection(CraftPlayer player) {
        this.channel = player.getHandle().playerConnection.networkManager.channel;
        this.player = player;
        this.inject();
    }

    private void inject() {
        try {
            ByteBufDecoderHandler handler = new ByteBufDecoderHandler(player);
            if(this.channel.pipeline().get("decompress") != null)
                this.channel.pipeline().addAfter("decompress", "ac-decoder", handler);
            else if(this.channel.pipeline().get("splitter") != null) {
                this.channel.pipeline().addAfter("splitter", "ac-decoder", handler);
            }
        }catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void unInject() {
        if(channel.pipeline().get("ac-decoder") != null) {
            channel.pipeline().remove("ac-decoder");
        }
    }
}
