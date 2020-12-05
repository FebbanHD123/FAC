package de.febanhd.anticrash.checks.impl.nbt;

import de.febanhd.anticrash.checks.CheckResult;
import de.febanhd.anticrash.config.ConfigCach;
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
    public CheckResult isValid(NBTTagCompound tag) {
        return ExploitCheckUtils.isValidMap(tag);
    }

    @Override
    public List<Material> material() {
        return Arrays.asList(Material.MAP, Material.EMPTY_MAP);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if(event.getPlayer().getItemInHand().getType() == Material.EMPTY_MAP) {
            ItemStack itemstack = event.getPlayer().getItemInHand();
            net.minecraft.server.v1_8_R3.ItemStack item = CraftItemStack.asNMSCopy(itemstack);
            Bukkit.getScheduler().scheduleSyncDelayedTask(AntiCrashPlugin.getPlugin(), () -> {
                if(itemstack.getType() == Material.MAP) {
                    if(item.getTag().getInt("range") < 0||item.getTag().getInt("range") > ConfigCach.getInstance().getValue("nbcheck.map.maxRange", 15, Integer.class)) {
                        itemstack.setType(Material.AIR);
                    }
                }
            }, 20);
        }
    }
}
