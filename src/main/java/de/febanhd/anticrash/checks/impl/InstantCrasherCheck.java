package de.febanhd.anticrash.checks.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import de.febanhd.anticrash.checks.AbstractCheck;
import org.bukkit.entity.Player;

public class InstantCrasherCheck extends AbstractCheck {

    public InstantCrasherCheck() {
        super("InstantCrasherCheck", PacketType.Handshake.Client.SET_PROTOCOL);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        if(player.getAddress() == null) {
            this.sendCrashWarning(player, event, "IP-Address is null. (InstantCrasher)");
        }
    }
}
