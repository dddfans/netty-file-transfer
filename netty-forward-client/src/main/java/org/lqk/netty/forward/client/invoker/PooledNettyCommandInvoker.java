package org.lqk.netty.forward.client.invoker;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang.StringUtils;
import org.lqk.netty.forward.protocol.*;
import org.lqk.netty.forward.protocol.future.NettyCommandFuture;
import org.lqk.netty.forward.protocol.future.NettyCommandFutureBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 支持以channelpool的方式使用channel
 */

public class PooledNettyCommandInvoker extends AbstractNettyCommandInvoker {

    private ChannelPoolMap<String, SimpleChannelPool> poolMap;

    private long timeout;

    private ChannelInitializer channelInitializer;

    private static Logger log = LoggerFactory.getLogger(PooledNettyCommandInvoker.class);

    public PooledNettyCommandInvoker(final Bootstrap bootstrap, ConcurrentHashMap<Integer, NettyCommandFuture> requestTable, long timeout,ChannelInitializer channelInitializer) {
        super(bootstrap, requestTable);
        this.poolMap = new AbstractChannelPoolMap<String, SimpleChannelPool>() {
            @Override
            protected SimpleChannelPool newPool(String key) {
                String[] address = key.split(":");
                return new SimpleChannelPool(bootstrap.remoteAddress(address[0],Integer.parseInt(address[1])), new DefaultChannelPoolHandler());
            }
        };
        this.timeout = timeout;
        this.channelInitializer = channelInitializer;
    }

    private class DefaultChannelPoolHandler implements ChannelPoolHandler {

        public void channelReleased(Channel ch) throws Exception {
            log.debug("channelReleased");
        }

        public void channelAcquired(Channel ch) throws Exception {
            log.debug("channelAcquired");
        }

        public void channelCreated(Channel ch) throws Exception {
            log.debug("channelCreated");
            //TODO handler提前绑定好像有问题，channelpool只是做了创建，并没有做相关的绑定，所以channel创建完毕后，必须再绑定一次
            ch.pipeline().addLast(channelInitializer);

        }
    }

    /**
     * 建立channel
     *
     * @param addr
     * @return
     */
    protected Channel createChannel(String addr) throws InterruptedException {
        final SimpleChannelPool pool = poolMap.get(addr);
        Future<Channel> f = pool.acquire();
        f.await(timeout, TimeUnit.MILLISECONDS);
        Channel channel = f.getNow();
        log.debug("channel isActive {},isOpen {} pipeline {} ",channel.isActive(),channel.isOpen(),channel.pipeline().toString());
        return channel;
    }

    /**
     * 关闭channel
     *
     * @param addr
     * @param channel
     */
    protected void closeChannel(String addr, Channel channel) {
        if (StringUtils.isEmpty(addr) || null == channel) {
            return;
        }
        final SimpleChannelPool pool = poolMap.get(addr);
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
