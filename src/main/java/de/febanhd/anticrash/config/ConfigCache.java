package de.febanhd.anticrash.config;

import com.google.common.collect.Maps;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class ConfigCache {

    @Getter
    private static ConfigCache instance;

    private FileConfiguration cfg;
    private HashMap<String, Object> values = Maps.newHashMap();

    public ConfigCache() {
        instance = this;
        JavaPlugin plugin = AntiCrashPlugin.getPlugin();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();

        this.cfg = plugin.getConfig();
    }

    public <T> T getValue(String key, T defaultValue, Class<T> typeOfT) {
        Object value = getValue(key);
        if(value == null)
            return defaultValue;
        T finalValue;
        try {
            finalValue = typeOfT.cast(value);
        }catch (ClassCastException | NullPointerException e) {
            finalValue = defaultValue;
        }
        return finalValue;
    }

    public Object getValue(String key) {
        if(this.values.containsKey(key)) {
            return this.values.get(key);
        }else {
            Object value = this.cfg.get(key);
            this.values.put(key, value);
            return value;
        }
    }

    public boolean isCheckEnable(String checkName) {
        if(this.cfg.contains("checks." + checkName)) {
            return this.cfg.getBoolean("checks." + checkName);
        }else {
            this.cfg.set("checks." + checkName, true);
            AntiCrashPlugin.getPlugin().saveConfig();
            return true;
        }
    }

    public boolean contains(String path) {
        return this.cfg.contains(path);
    }
}
