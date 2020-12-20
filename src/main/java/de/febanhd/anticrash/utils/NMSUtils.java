package de.febanhd.anticrash.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSUtils {

    public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    public static Object getConnection(Player player) throws Exception {
        Method getHandle = player.getClass().getMethod("getHandle");
        Object nmsPlayer = getHandle.invoke(player);
        Field conField = nmsPlayer.getClass().getField("playerConnection");
        Object con = conField.get(nmsPlayer);
        return con;
    }

    public static Object getChannel(Player player) throws Exception {
        Object playerConnection = getConnection(player);
        Field networkManageField = playerConnection.getClass().getField("networkManager");
        Object networkManager = networkManageField.get(playerConnection);
        Field channelField = networkManager.getClass().getField("channel");
        Object channel = channelField.get(networkManager);
        return channel;
    }
}
