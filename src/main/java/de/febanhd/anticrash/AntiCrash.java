package de.febanhd.anticrash;

import com.google.common.collect.Lists;
import de.febanhd.anticrash.checks.ICheck;
import de.febanhd.anticrash.checks.impl.*;
import de.febanhd.anticrash.checks.impl.nbt.NBTTagCheck;
import de.febanhd.anticrash.config.ConfigCache;
import de.febanhd.anticrash.player.PlayerCash;
import de.febanhd.anticrash.utils.TPSCalculator;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class AntiCrash {

    public static final String PREFIX = "§8[§cFAC§8] §r";

    public static void init(JavaPlugin plugin) {
         new AntiCrash(plugin);
    }
    private static AntiCrash instance;

    public static AntiCrash getInstance() {
        return instance;
    }

    private final Plugin plugin;
    private final CopyOnWriteArrayList<ICheck> checks = Lists.newCopyOnWriteArrayList();

    private TPSCalculator tpsCalculator;
    private PlayerCash playerCash;

    private AntiCrash(Plugin plugin) {
        instance = this;
        new ConfigCache();

        this.plugin = plugin;
        this.tpsCalculator = new TPSCalculator();
        this.playerCash = new PlayerCash();

        this.registerChecks();

        Bukkit.getOnlinePlayers().forEach(player -> playerCash.register(player));
    }

    private void registerChecks() {
        this.plugin.getLogger().info("Register Checks");

        this.checks.add(new BookCheck());
        this.checks.add(new WindowClickCheck());
        this.checks.add(new BlockPlaceCheck());
        this.checks.add(new MoveCheck());
        this.checks.add(new NBTTagCheck());
        this.checks.add(new InstantCrasherCheck());
        this.checks.add(new DosCheck());
        this.checks.add(new SignCheck());
        this.checks.add(new EnderPortalCheck());

    }

    public CopyOnWriteArrayList<ICheck> getChecks() {
        return checks;
    }

    public ICheck getCheck(Class<? extends ICheck> clazz) {
        for(ICheck check : this.checks) {
            if(check.getClass().equals(clazz)) {
                return check;
            }
        }
        return null;
    }
}
