package de.febanhd.anticrash.checks.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.checks.CheckResult;
import de.febanhd.anticrash.config.ConfigCache;
import de.febanhd.anticrash.utils.ExploitCheckUtils;
import net.minecraft.server.v1_8_R3.ItemStack;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

public class WindowClickCheck extends AbstractCheck {
    public WindowClickCheck() {
        super("WindowClickCheck", PacketType.Play.Client.WINDOW_CLICK, PacketType.Play.Client.SET_CREATIVE_SLOT);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();
        org.bukkit.inventory.ItemStack bukkitStack = packet.getItemModifier().readSafely(0);
        if(bukkitStack.getType().toString().contains("BOOK")) {
            if(ConfigCache.getInstance().getValue("bookcheck.disableBooks", false, Boolean.class)) {
                event.setCancelled(true);
                return;
            }
            ItemStack stack = CraftItemStack.asNMSCopy(bukkitStack);

            CheckResult result = ExploitCheckUtils.isInvalidBookTag(stack.getTag());
            if(result.check()) {
                this.sendCrashWarning(player, event, result.getReason());
                return;
            }
        }

    }
}
