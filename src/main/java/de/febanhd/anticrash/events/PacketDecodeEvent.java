package de.febanhd.anticrash.events;

import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PacketDecodeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();


    private final Player player;
    private final ByteBuf byteBuf;
    private boolean canceled;

    public PacketDecodeEvent(boolean isAsync, Player player, ByteBuf byteBuf) {
        super(isAsync);
        this.player = player;
        this.byteBuf = byteBuf;
        this.canceled = false;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void setCancelled(boolean canceled) {
        this.canceled = canceled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public ByteBuf getByteBuf() {
        return byteBuf;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
