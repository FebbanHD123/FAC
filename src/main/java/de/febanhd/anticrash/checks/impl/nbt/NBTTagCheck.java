package de.febanhd.anticrash.checks.impl.nbt;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.Lists;
import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.checks.CheckResult;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class NBTTagCheck extends AbstractCheck {

    private ArrayList<INBTCheck> checks = Lists.newArrayList();

    public NBTTagCheck() {
        super("NBTTagCheck", PacketType.Play.Client.BLOCK_PLACE, PacketType.Play.Client.SET_CREATIVE_SLOT);

        this.checks.add(new MobSpawnerNBTCheck());
        this.checks.add(new FurnaceNBTCheck());
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();
        ItemStack stack = packet.getItemModifier().readSafely(0);
        Material itemType = stack.getType();
        if(itemType == Material.AIR) return;
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound nbtTagCompound = nmsStack.getTag();
        if(nbtTagCompound != null) {
            this.checks.forEach(check -> {
                if(check.material().contains(itemType)) {
                    CheckResult result = check.isValid(nbtTagCompound);
                    if(!result.check()) {
                        player.getInventory().clear();
                        this.sendCrashWarning(player, event, "Invalid NBT-Tag: " + result.getReason() + "!");
                        event.setCancelled(true);
                    }
                }
            });
        }
    }

    public ArrayList<INBTCheck> getChecks() {
        return checks;
    }
}
