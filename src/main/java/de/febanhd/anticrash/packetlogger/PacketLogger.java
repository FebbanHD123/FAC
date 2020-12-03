package de.febanhd.anticrash.packetlogger;

import de.febanhd.anticrash.nettyinjections.PacketInjection;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInKeepAlive;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Getter
public class PacketLogger implements PacketInjection.PacketListener {

    private final File DIR = new File(AntiCrashPlugin.getPlugin().getDataFolder().getAbsoluteFile() + "/packetLogs");
    private static final List<Class<? extends Packet<?>>> IGNORE_PACKETS = Arrays.asList(PacketPlayInFlying.class, PacketPlayInFlying.PacketPlayInPosition.class, PacketPlayInKeepAlive.class, PacketPlayInArmAnimation.class, PacketPlayInFlying.PacketPlayInLook.class, PacketPlayInFlying.PacketPlayInPositionLook.class);
    private Player player;
    private PacketLoggerFileWriter packetLoggerFileWriter;
    private PacketInjection packetInjection;
    private boolean logging = false;

    public PacketLogger(Player player, PacketInjection packetInjection) {
        this.player = player;
        this.packetInjection = packetInjection;
        if(!DIR.exists()) this.DIR.mkdir();
        this.packetLoggerFileWriter = new PacketLoggerFileWriter(this);
        this.packetInjection.addPacketListener(this);
    }

    @Override
    public boolean handlePacket(Player player, final Packet<?> packet) {
        if(!logging) return true;
        final PacketLoggerPacket packetLoggerPacket = new PacketLoggerPacket(packet);

        Class<?> packetClass = packet.getClass();
        if(PacketLogger.IGNORE_PACKETS.contains(packetClass)) return true;
        while(packetClass != null) {
            Arrays.stream(packet.getClass().getDeclaredFields()).forEach(field -> {
                try {
                    if(!field.isAccessible())
                        field.setAccessible(true);
                    String type;
                    if(field.getType().isPrimitive())
                        type = field.getType().getName();
                    else
                        type = field.getType().getCanonicalName();

                    try {
                        Object value = field.get(packet);
                        if(field.getType().isArray() && value != null)
                            value = Arrays.toString((Object[]) value);
                        packetLoggerPacket.getFields().put(field.getName(), new PacketField(type, value));
                    }catch (ReflectiveOperationException | ClassCastException e) {
                        packetLoggerPacket.getFields().put(field.getName(), new PacketField(type, "?"));
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            });
            packetClass = packetClass.getSuperclass();
            if(packetClass == Object.class) {
                packetClass = null;
            }
        }
        this.packetLoggerFileWriter.writePacket(packetLoggerPacket);
        return true;
    }

    public void startLogging() {
        this.logging = true;
        this.packetLoggerFileWriter.start();
    }

    public void stopLogging() {
        this.logging = false;
        this.packetLoggerFileWriter.stop();
    }
}
