package de.febanhd.anticrash.packetlogger;

import com.google.common.collect.Maps;
import net.minecraft.server.v1_8_R3.Packet;

import java.util.Map;

public class PacketLoggerPacket {

    private Map<String, Map<String, PacketField>> fields = Maps.newHashMap();
    private Packet<?> packet;

    public PacketLoggerPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public Map<String, PacketField> getFields() {
        return fields.computeIfAbsent(packet.getClass().getCanonicalName(), key -> Maps.newHashMap());
    }
}
