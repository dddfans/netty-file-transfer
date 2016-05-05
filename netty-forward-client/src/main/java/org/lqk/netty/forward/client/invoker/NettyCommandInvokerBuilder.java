package org.lqk.netty.forward.client.invoker;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import org.lqk.netty.forward.client.invoker.pool.PooledNettyCommandInvoker;
import org.lqk.netty.forward.client.invoker.simple.SimpleNettyCommandInvoker;
import org.lqk.netty.forward.protocol.future.NettyCommandFuture;
import org.lqk.netty.zk.client.ServerAddressProvider;
import org.lqk.netty.zk.client.ServerServiceAddressProvider;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bert on 16-5-5.
 */
public class NettyCommandInvokerBuilder {
    public static NettyCommandInvoker create(Bootstrap bootstrap, ConcurrentHashMap<Integer, NettyCommandFuture> requestTable) {
        return new SimpleNettyCommandInvoker(bootstrap,requestTable);
    }
    public static NettyCommandInvoker create(final Bootstrap bootstrap, ConcurrentHashMap<Integer, NettyCommandFuture> requestTable, ChannelInitializer channelInitializer, long acquireTimeoutMillis) {
        return new PooledNettyCommandInvoker(bootstrap,requestTable,channelInitializer,acquireTimeoutMillis);
    }
    public static NettyCommandInvoker create(final Bootstrap bootstrap, ConcurrentHashMap<Integer, NettyCommandFuture> requestTable,
                                             ChannelInitializer channelInitializer, long acquireTimeoutMillis, int maxConnections, int maxPendingAcquires) {
        return new PooledNettyCommandInvoker(bootstrap,requestTable,channelInitializer,acquireTimeoutMillis,maxConnections,maxPendingAcquires);
    }

    public static NettyCommandInvoker create(final Bootstrap bootstrap, ConcurrentHashMap<Integer, NettyCommandFuture> requestTable,
                                             ChannelInitializer channelInitializer, long acquireTimeoutMillis, int maxConnections, int maxPendingAcquires,ServerAddressProvider serverAddressProvider) {
        return new PooledNettyCommandInvoker(bootstrap,requestTable,channelInitializer,acquireTimeoutMillis,maxConnections,maxPendingAcquires,serverAddressProvider);
    }
}
