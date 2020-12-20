package de.febanhd.anticrash.checks.impl.nbt;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import de.febanhd.anticrash.checks.CheckResult;
import de.febanhd.anticrash.utils.ExploitCheckUtils;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class SkullNBTCheck implements INBTCheck {

    @Override
    public CheckResult isValid(NbtCompound tag) {
        CheckResult isInvalidResult = ExploitCheckUtils.isInvalidSkull(tag);
        if(isInvalidResult.check()) {
            return new CheckResult.Negative(isInvalidResult.getReason());
        }
        return new CheckResult.Positive();
    }

    @Override
    public List<Material> material() {
        return Arrays.asList(Material.SKULL, Material.SKULL_ITEM);
    }
}
