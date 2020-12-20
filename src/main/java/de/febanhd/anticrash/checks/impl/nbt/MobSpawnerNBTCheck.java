package de.febanhd.anticrash.checks.impl.nbt;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import de.febanhd.anticrash.checks.CheckResult;
import de.febanhd.anticrash.utils.ExploitCheckUtils;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class MobSpawnerNBTCheck implements INBTCheck {

    @Override
    public CheckResult isValid(final NbtCompound spawnerTag) {
        return ExploitCheckUtils.isValidSpawnerEntityTag(spawnerTag.getCompound("BlockEntityTag"));
    }

    @Override
    public List<Material> material() {
        return Arrays.asList(Material.MOB_SPAWNER);
    }
}
