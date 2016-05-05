package org.lqk.netty.forward.client.invoker;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.pool.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by bert on 16-5-5.
 */
public class SimpleChannelPoolMap extends AbstractChannelPoolMap<InetSocketAddress, ChannelPool> {
    private ChannelPoolHandler channelPoolHandler;
    private Bootstrap bootstrap;

    private int maxConnections = -1;
    private int maxPendingAcquires = -1;
    private long acquireTimeoutMillis = -1;

    private boolean isFixed = false;

    public SimpleChannelPoolMap(Bootstrap bootstrap,ChannelPoolHandler channelPoolHandler){
        this.bootstrap = bootstrap;
        this.channelPoolHandler =  channelPoolHandler;
    }

    public SimpleChannelPoolMap(Bootstrap bootstrap, ChannelPoolHandler channelPoolHandler, long acquireTimeoutMillis, int maxConnections, int maxPendingAcquires) {
        this(bootstrap, channelPoolHandler);
        this.acquireTimeoutMillis = acquireTimeoutMillis;
        this.maxConnections = maxConnections;
        this.maxPendingAcquires = maxPendingAcquires;
        this.isFixed = true;
    }

    @Override
    public ChannelPool newPool(InetSocketAddress address) {
        if (isFixed) {
            return new FixedChannelPool(bootstrap.remoteAddress(address),
                    channelPoolHandler,
                    ChannelHealthChecker.ACTIVE, FixedChannelPool.AcquireTimeoutAction.FAIL,
                    acquireTimeoutMillis, maxConnections, maxPendingAcquires);
        }
        return new SimpleChannelPool(bootstrap.remoteAddress(address), channelPoolHandler);

    }

}
