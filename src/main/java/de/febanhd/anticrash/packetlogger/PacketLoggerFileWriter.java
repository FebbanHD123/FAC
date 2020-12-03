package de.febanhd.anticrash.packetlogger;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class PacketLoggerFileWriter implements Runnable {

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Thread thread;
    private final File file;
    private PacketLogger packetLogger;
    private boolean logging = false;

    private final CopyOnWriteArrayList<PacketLoggerPacket> packetQueue = Lists.newCopyOnWriteArrayList();

    public PacketLoggerFileWriter(PacketLogger packetLogger) {
        this.packetLogger = packetLogger;
        File dir = packetLogger.getDIR();
        String fileName = packetLogger.getPlayer().getName() + "_" + simpleDateFormat.format(new Date()) + ".txt";
        this.file = new File(dir, fileName);
        this.thread = new Thread(this,"PacketLoggerThread");
    }

    public void writePacket(PacketLoggerPacket packet) {
        this.packetQueue.add(packet);
    }

    public void start() {
        this.logging = true;
        this.thread.start();
    }

    public void stop() {
        this.logging = false;
    }

    @SneakyThrows
    @Override
    public void run() {
        while(this.logging) {
            if(!packetQueue.isEmpty()) {
                FileWriter fileWriter = new FileWriter(this.file);
                ArrayList<PacketLoggerPacket> editPackets = Lists.newArrayList();
                if(!this.file.exists()) this.file.createNewFile();
                String fileContent = new String(Files.readAllBytes(this.file.toPath()));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(fileContent);
                for (int i = 0; i < packetQueue.size(); i++) {
                    PacketLoggerPacket packetLoggerPacket = packetQueue.get(i);
                    editPackets.add(packetLoggerPacket);
                    String packetName = packetLoggerPacket.getPacket().getClass().getSimpleName();
                    String fields = packetLoggerPacket.getFields().toString();
                    stringBuilder.append("\n").append(packetName).append("\n").append(fields);
                }
                String content = stringBuilder.toString();
                fileWriter.write(content);
                fileWriter.flush();
                packetQueue.removeAll(editPackets);
            }
        }
    }
}
