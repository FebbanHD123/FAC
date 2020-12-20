package de.febanhd.anticrash.checks.impl.nbt;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import de.febanhd.anticrash.checks.CheckResult;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import de.febanhd.anticrash.utils.ExploitCheckUtils;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class MapNBTCheck implements INBTCheck, Listener {

    public MapNBTCheck() {
        Bukkit.getPluginManager().registerEvents(this, AntiCrashPlugin.getPlugin());
    }

    @Override
    public CheckResult isValid(NbtCompound tag) {
        return ExploitCheckUtils.isValidMap(tag);
    }

    @Override
    public List<Material> material() {
        return Arrays.asList(Material.MAP, Material.EMPTY_MAP);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        ItemStack itemInHand = event.getItem();
        if (itemInHand == null || !itemInHand.getType().equals(Material.EMPTY_MAP)) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(AntiCrashPlugin.getPlugin(), () -> {
            if (itemInHand.getType() == Material.MAP) {
                NbtCompound tag = (NbtCompound) NbtFactory.fromItemTag(itemInHand);
                if (!ExploitCheckUtils.isValidMap(tag).check()) {
                    itemInHand.setType(Material.AIR);
                }
            }
        }, 20);
    }
}
