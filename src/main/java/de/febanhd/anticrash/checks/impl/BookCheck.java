package de.febanhd.anticrash.checks.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import de.febanhd.anticrash.checks.AbstractCheck;
import de.febanhd.anticrash.checks.CheckResult;
import de.febanhd.anticrash.config.ConfigCache;
import de.febanhd.anticrash.utils.ExploitCheckUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.io.IOException;

public class BookCheck extends AbstractCheck {

    public BookCheck() {
        super("BookCheck", PacketType.Play.Client.CUSTOM_PAYLOAD, PacketType.Play.Client.BLOCK_PLACE);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        if(!player.isOnline()) {
            event.setCancelled(true);
            return;
        }
        PacketContainer packet = event.getPacket();
        if(packet.getType().equals(PacketType.Play.Client.CUSTOM_PAYLOAD)) {
            StructureModifier<ByteBuf> byteBufModifier = event.getPacket().getSpecificModifier(ByteBuf.class);
            ByteBuf byteBuf = byteBufModifier.readSafely(0).copy();

            String channel = packet.getStrings().readSafely(0);

            if (channel == null || channel.isEmpty()) {
                this.sendCrashWarning(player, event, "Empty channel");
                return;
            }

            if (channel.equals("MC|BSign") || channel.equals("MC|BEdit") || channel.equals("MC|BOpen")) {

                if(ConfigCache.getInstance().getValue("bookcheck.disableBooks", false, Boolean.class)) {
                    event.setCancelled(true);
                    return;
                }

                if (!player.getItemInHand().getType().toString().contains("BOOK")) {
                    this.sendInvalidPacketWarning(player, event, "Player send a book packet without holding a book in his hand.");
                    return;
                }

                if(byteBuf.capacity() < 1) {
                    event.setCancelled(true);
                    return;
                }

                try {

                    ItemStack stack = null;
                    short short0 = byteBuf.readShort();
                    if (short0 >= 0) {
                        byte b0 = byteBuf.readByte();
                        short short1 = byteBuf.readShort();
                        stack = new ItemStack(Item.getById(short0), b0, short1);

                        NBTTagCompound tag;

                        int i = byteBuf.readerIndex();
                        byte b1 = byteBuf.readByte();
                        if (b1 == 0) {
                            tag = null;
                        } else {
                            byteBuf.readerIndex(i);
                            tag = NBTCompressedStreamTools.a(new ByteBufInputStream(byteBuf), new NBTReadLimiter(2097152L));
                        }

                        stack.setTag(tag);
                    }

                    if (!ItemWrittenBook.b(stack.getTag())) {
                        this.sendCrashWarning(player, event, "Invalid book arguments");
                    }

                    CheckResult result = ExploitCheckUtils.isInvalidBookTag(stack.getTag());
                    if (result.check()) {
                        this.sendCrashWarning(player, event, "Invalid book tag: " + result.getReason() + "!");
                        return;
                    }

                    try {
                        if (channel.equals("MC|BSign") && !stack.getTag().getString("author").equals(player.getName())) {
                            this.sendCrashWarning(player, event, "Sign book with another author (author: " + stack.getTag().getString("author") + ")");
                            return;
                        }
                    } catch (Exception e) {
                    }


                } catch (IOException e) {
                    this.sendCrashWarning(player, "Error while serialize book: " + e.getClass().getSimpleName());
                }
            }
        } else if (packet.getType().equals(PacketType.Play.Client.BLOCK_PLACE)) {
            org.bukkit.inventory.ItemStack bukkitStack = packet.getItemModifier().readSafely(0);
            ItemStack stack = CraftItemStack.asNMSCopy(bukkitStack);
            if(bukkitStack.getType().toString().contains("BOOK")) {
                if(ConfigCache.getInstance().getValue("bookcheck.disableBooks", false, Boolean.class)) {
                    event.setCancelled(true);
                    return;
                }
                CheckResult result = ExploitCheckUtils.isInvalidBookTag(stack.getTag());
                if(result.check()) {
                    this.sendCrashWarning(player, event, "Open book with invalid tag: " + result.getReason());
                    return;
                }
            }
        }
    }
}
