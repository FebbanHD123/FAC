package de.febanhd.anticrash.checks.impl.nbt;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import de.febanhd.anticrash.checks.CheckResult;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;

import java.util.List;

public interface INBTCheck {

    CheckResult isValid(NbtCompound tag);

    List<Material> material();
}
