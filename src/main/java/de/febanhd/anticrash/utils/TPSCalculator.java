package de.febanhd.anticrash.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.febanhd.anticrash.plugin.AntiCrashPlugin;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TPSCalculator {

    public HashMap<Long, Integer> tpsCash = Maps.newHashMap();

    private long startMillis = 0;
    private int currentTicks = 0;
    private int currentTps = -1;

    public TPSCalculator() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(AntiCrashPlugin.getPlugin(), () -> {
            if(startMillis + 1000 <= System.currentTimeMillis()) {
                currentTps = currentTicks;
                tpsCash.put(this.startMillis, this.currentTps);
                startMillis = System.currentTimeMillis();
                currentTicks = 0;
                List<Long> valuesToRemove = Lists.newArrayList();
                this.tpsCash.forEach((aLong, integer) -> {
                    if(aLong <= System.currentTimeMillis() - 60000) {
                        valuesToRemove.add(aLong);
                    }
                });
                if(!valuesToRemove.isEmpty()) {
                    valuesToRemove.forEach(tpsCash::remove);
                }
            }
            currentTicks++;
        }, 0, 1);
    }

    public int getCurrentTps() {
        return currentTps;
    }

    public double getAverageTps(int seconds) {
        long time = seconds * 1000;
        ArrayList<Integer> list = Lists.newArrayList();
        this.tpsCash.forEach((aLong, integer) -> {
            if(aLong > System.currentTimeMillis() - time) {
                list.add(integer);
            }
        });
        double value = 0;
        for(int i = 0; i < list.size(); i++) {
            value += list.get(i);
        }
        return value / list.size();
    }
}
