package de.febanhd.anticrash.nettyinjections;

import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.injector.netty.BootstrapList;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Lists;
import de.febanhd.anticrash.checks.impl.DosCheck;
import de.febanhd.anticrash.handler.ChannelClosedHandler;
import io.netty.channel.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public class MCChannelInjection {

    private List<VolatileField> bootstrapFields;
    private volatile List<Object> networkManagers;
    private final DosCheck dosCheck;

    /*
     * The injection is from ProtocolLib :D Thanks
     */

    public MCChannelInjection(DosCheck dosCheck) throws ReflectiveOperationException {
        this.dosCheck = dosCheck;

        ChannelInboundHandler beginInitProtocol = new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel channel) throws Exception {
                if (dosCheck.isBlocked(channel)) {
                    try {
                        dosCheck.getConnectionList().add(new DosCheck.Connection(channel));
                    } catch (NullPointerException e) {
                    }
                    channel.pipeline().addLast(new ChannelClosedHandler());
                    if (dosCheck.isAttack) {
                        dosCheck.blockedConnections++;
                    }
                    return;
                }
                channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                        if (dosCheck.handleObject(channelHandlerContext, o)) {
                            super.channelRead(channelHandlerContext, o);
                        } else {
                            channelHandlerContext.close();
                        }
                    }

                    @Override
                    public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {
                        if (dosCheck.initChannel(channelHandlerContext)) {
                            super.channelRegistered(channelHandlerContext);
                        } else {
                            channelHandlerContext.close();
                        }
                    }
                });
            }

        };

        final ChannelHandler connectionHandler = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                Channel channel = (Channel) msg;
                channel.pipeline().addFirst(beginInitProtocol);
                ctx.fireChannelRead(msg);
            }
        };
        FuzzyReflection fuzzyServer = FuzzyReflection.fromClass(MinecraftReflection.getMinecraftServerClass());

        List<Method> serverConnectionMethods = fuzzyServer.getMethodListByParameters(MinecraftReflection.getServerConnectionClass(), new Class[]{});

        Object server = fuzzyServer.getSingleton();
        Object serverConnection = null;

        for (Method method : serverConnectionMethods) {
            try {
                serverConnection = method.invoke(server);

                // Continue until we get a server connection
                if (serverConnection != null) {
                    break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (serverConnection == null) {
            throw new ReflectiveOperationException("Failed to obtain server connection");
        }

        FuzzyReflection fuzzy = FuzzyReflection.fromObject(serverConnection, true);

        try {
            Field field = fuzzy.getParameterizedField(List.class, MinecraftReflection.getNetworkManagerClass());
            field.setAccessible(true);

            networkManagers = (List<Object>) field.get(serverConnection);
        } catch (Exception ex) {
            ProtocolLogger.debug("Encountered an exception checking list fields", ex);

            Method method = fuzzy.getMethodByParameters("getNetworkManagers", List.class,
                    new Class<?>[]{serverConnection.getClass()});
            method.setAccessible(true);

            networkManagers = (List<Object>) method.invoke(null, serverConnection);
        }

        if (networkManagers == null) {
            throw new ReflectiveOperationException("Failed to obtain list of network managers");
        }

        bootstrapFields = this.getBootstrapFields(serverConnection);

        for (VolatileField field : bootstrapFields) {
            final List<Object> list = (List<Object>) field.getValue();

            // We don't have to override this list
            if (list == networkManagers) {
                continue;
            }

            // Synchronize with each list before we attempt to replace them.
            field.setValue(new BootstrapList(list, connectionHandler));
        }
    }

    private List<VolatileField> getBootstrapFields(Object serverConnection) {
        List<VolatileField> result = Lists.newArrayList();

        // Find and (possibly) proxy every list
        for (Field field : FuzzyReflection.fromObject(serverConnection, true).getFieldListByType(List.class)) {
            VolatileField volatileField = new VolatileField(field, serverConnection, true).toSynchronized();

            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) volatileField.getValue();

            if (list.size() == 0 || list.get(0) instanceof ChannelFuture) {
                result.add(volatileField);
            }
        }

        return result;
    }

    public synchronized void close() {
        VolatileField field;
        for (Iterator var1 = this.bootstrapFields.iterator(); var1.hasNext(); field.revertValue()) {
            field = (VolatileField) var1.next();
            Object value = field.getValue();
            if (value instanceof BootstrapList) {
                ((BootstrapList) value).close();
            }
        }

    }

}
