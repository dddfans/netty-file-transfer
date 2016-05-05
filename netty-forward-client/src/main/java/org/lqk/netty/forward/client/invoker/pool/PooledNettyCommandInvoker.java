package org.lqk.netty.forward.client.invoker.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.pool.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang.StringUtils;
import org.lqk.netty.forward.client.invoker.AbstractNettyCommandInvoker;
import org.lqk.netty.forward.client.invoker.SimpleChannelPoolMap;
import org.lqk.netty.forward.protocol.*;
import org.lqk.netty.forward.protocol.future.NettyCommandFuture;
import org.lqk.netty.forward.protocol.future.NettyCommandFutureBuilder;
import org.lqk.netty.zk.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 支持以channelpool的方式使用channel
 */

public class PooledNettyCommandInvoker extends AbstractNettyCommandInvoker {

    protected ChannelPoolMap<InetSocketAddress, ChannelPool> poolMap;

    protected ServerAddressConverter serverAddressConverter;

    protected long acquireTimeoutMillis;

    private static Logger log = LoggerFactory.getLogger(PooledNettyCommandInvoker.class);

    public PooledNettyCommandInvoker(final Bootstrap bootstrap, ConcurrentHashMap<Integer, NettyCommandFuture> requestTable,ChannelInitializer channelInitializer,long acquireTimeoutMillis) {
        super(bootstrap, requestTable);
        this.acquireTimeoutMillis = acquireTimeoutMillis;
        this.poolMap = new SimpleChannelPoolMap(bootstrap,new SimpleChannelPoolHandler(channelInitializer));
        this.serverAddressConverter = new SimpleServerAddressConverter();
    }

    public PooledNettyCommandInvoker(final Bootstrap bootstrap, ConcurrentHashMap<Integer, NettyCommandFuture> requestTable,
                                          ChannelInitializer channelInitializer, long acquireTimeoutMillis, int maxConnections, int maxPendingAcquires) {
        super(bootstrap, requestTable);
        this.acquireTimeoutMillis = acquireTimeoutMillis;
        this.poolMap = new SimpleChannelPoolMap(bootstrap,new SimpleChannelPoolHandler(channelInitializer),acquireTimeoutMillis,maxConnections,maxPendingAcquires);
        this.serverAddressConverter = new SimpleServerAddressConverter();
    }

    public PooledNettyCommandInvoker(final Bootstrap bootstrap, ConcurrentHashMap<Integer, NettyCommandFuture> requestTable,
                                     ChannelInitializer channelInitializer, long acquireTimeoutMillis, int maxConnections, int maxPendingAcquires, ServerAddressProvider serverAddressProvider) {
        super(bootstrap, requestTable);
        this.acquireTimeoutMillis = acquireTimeoutMillis;
        this.poolMap = new SimpleChannelPoolMap(bootstrap,new SimpleChannelPoolHandler(channelInitializer),acquireTimeoutMillis,maxConnections,maxPendingAcquires);
        this.serverAddressConverter = new ZookeeperServerAddressConverter(serverAddressProvider);
    }

    /**
     * 建立channel
     *
     * @param key
     * @return
     */
    protected Channel createChannel(String key) throws InterruptedException {
        InetSocketAddress address = serverAddressConverter.convert(key);
        final ChannelPool pool = poolMap.get(address);
        Future<Channel> f = pool.acquire();
        f.await(acquireTimeoutMillis, TimeUnit.MILLISECONDS);
        Channel channel = f.getNow();
        log.debug("channel isActive {},isOpen {} pipeline {} ",channel.isActive(),channel.isOpen(),channel.pipeline().toString());
        return channel;
    }

    /**
     * 关闭channel
     *
     * @param key
     * @param channel
     */
    protected void closeChannel(String key, Channel channel) {
        if (StringUtils.isEmpty(key) || null == channel) {
            return;
        }
        InetSocketAddress address = serverAddressConverter.convert(key);
        final ChannelPool pool = poolMap.get(address);
        if(null == pool){
            return;
        }
        pool.release(channel);
    }

    /*
     * 异步递送消息，并保证得到应答
     */
    public void invokeAsync(final Channel channel, final NettyCommand request, long timeoutMills,
                            NettyCommandCallBack callBack) throws InterruptedException {
        final NettyCommandFuture nettyCommandFuture = NettyCommandFutureBuilder.createAsynFuture(request.getOpaque(), timeoutMills, true,
                null, channel, callBack);
        //TODO 异常处理
        super.doInvokeAsync(channel, request, nettyCommandFuture);
    }


    /*
     * 单向递送消息
     */
    public void invokeOneway(final Channel channel, NettyCommand request, long timeoutMills)
            throws InterruptedException {
        try {
            NettyCommandBody body = request.getBody();
            if (body instanceof FileSegmentRequest) {
                FileSegmentRequest f = (FileSegmentRequest) body;
                log.debug("position {},blocksize {},filename {},crc32 {}", f.getPosition(), f.getBlockSize(),
                        f.getFileName(), f.getCrc32());

            }
            channel.writeAndFlush(request).addListener(new GenericFutureListener<ChannelFuture>() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    //TODO 回收
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
