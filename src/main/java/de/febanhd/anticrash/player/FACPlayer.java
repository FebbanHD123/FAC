package de.febanhd.anticrash.player;

import de.febanhd.anticrash.AntiCrash;
import de.febanhd.anticrash.nettyinjections.NettyDecodeInjection;
import de.febanhd.anticrash.nettyinjections.PacketInjection;
import de.febanhd.anticrash.packetlogger.PacketLogger;
import lombok.Getter;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import java.util.UUID;

@Getter
public class FACPlayer {

    private final long joinedAt;
    private final UUID uuid;

    private PacketInjection packetInjection;
    private NettyDecodeInjection nettyDecodeInjection;

    private PacketLogger packetLogger;

    public FACPlayer(long joinedAt, CraftPlayer player) {
        this.joinedAt = joinedAt;
        this.uuid = player.getUniqueId();
        this.packetInjection = new PacketInjection(player);
        this.nettyDecodeInjection = new NettyDecodeInjection(player);
        this.packetLogger = new PacketLogger(player, this.packetInjection);
//        this.packetLogger.startLogging();

        this.packetInjection.inject();

        AntiCrash.getInstance().getChecks().forEach(check -> check.registerFACPlayer(this));
    }

    public void unregister() {
        this.packetInjection.remove();
        this.nettyDecodeInjection.unInject();
//        this.packetLogger.stopLogging();
    }
}
