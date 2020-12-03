package de.febanhd.anticrash.checks.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.config.ConfigCach;
import org.bukkit.entity.Player;

public class SignCheck extends AbstractCheck {
    public SignCheck() {
        super("SignCheck", PacketType.Play.Client.UPDATE_SIGN);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Player player = event.getPlayer();
        WrappedChatComponent[] lines = packet.getChatComponentArrays().readSafely(0);
        for(int i = 0; i < lines.length; i++) {
            WrappedChatComponent chatComponent = lines[i];
            int length = chatComponent.getJson().length() - 2;
            int maxLength = ConfigCach.getInstance().getValue("signcheck.maxLength", 35, Integer.class);
            if(length > maxLength) {
                this.sendCrashWarning(player, event, "Line " + (i + 1) + " is too long (> " + maxLength + ")");
                return;
            }
        }
    }
}
