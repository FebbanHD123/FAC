package de.febanhd.anticrash.checks.impl.nbt;

import de.febanhd.anticrash.checks.CheckResult;
import de.febanhd.anticrash.utils.ExploitCheckUtils;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class FurnaceNBTCheck implements INBTCheck {

    @Override
    public CheckResult isValid(NBTTagCompound furnaceTag) {
        if(furnaceTag.hasKey("BlockEntityTag")) {
            NBTTagCompound tag = furnaceTag.getCompound("BlockEntityTag");
            if(tag.hasKey("Items")) {
                NBTTagList items = tag.getList("Items", 10);
                for(int i = 0; i < items.size(); i++) {
                    NBTTagCompound item = items.get(i);
                    String id = item.getString("id");
                    if(id.equals("minecraft:mob_spawner")) {
                        if (item.hasKey("tag")) {
                            NBTTagCompound spawnerTag = item.getCompound("tag");
                            if(spawnerTag.hasKey("BlockEntityTag")) {
                                CheckResult result = ExploitCheckUtils.isValidSpawnData(spawnerTag.getCompound("BlockEntityTag"));
                                if(!result.check()) {
                                    return new CheckResult.Negative("Contains invalid Items (" + result.getReason() + ")");
                                }
                            }
                        }
                    }
                }
            }
        }
        return new CheckResult.Positive("");
    }

    @Override
    public List<Material> material() {
        return Arrays.asList(Material.FURNACE);
    }
}
