package de.febanhd.anticrash.checks.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.febanhd.anticrash.AntiCrash;
import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.config.ConfigCache;
import net.minecraft.server.v1_8_R3.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceCheck extends AbstractCheck {

    public BlockPlaceCheck() {
        super("BlockPlaceCheck", PacketType.Play.Client.BLOCK_PLACE);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Player player = event.getPlayer();

        ItemStack stack = packet.getItemModifier().readSafely(0);

        if(stack == null) {
            this.sendCrashWarning(player, event, "Try to place block of null");
            return;
        }

        BlockPosition blockPosition = packet.getSpecificModifier(BlockPosition.class).readSafely(0);
        World world = player.getWorld();

        Location location = new Location(world, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());

        if(location.getX() < 16 && location.getZ() < 16) return;

        double dinstance = location.distance(player.getLocation());

        double maxDistance = ConfigCache.getInstance().getValue("placecheck.maxDistance", 32, Integer.class);

        if(dinstance > maxDistance && AntiCrash.getInstance().getTpsCalculator().getCurrentTps() > ConfigCache.getInstance().getValue("placecheck.maxTps", 15, Integer.class)) {
            this.sendCrashWarning(player, event, "The location of the placed block is too far away (> " + maxDistance + ")");
            return;
        }

    }
}
