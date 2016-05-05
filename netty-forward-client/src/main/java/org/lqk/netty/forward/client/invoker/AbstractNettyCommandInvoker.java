package org.lqk.netty.forward.client.invoker;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import org.lqk.netty.forward.protocol.*;
import org.lqk.netty.forward.protocol.future.NettyCommandFuture;
import org.lqk.netty.forward.protocol.future.NettyCommandFutureBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供基于netty的异步执行代码的抽象
 */

public abstract class AbstractNettyCommandInvoker implements NettyCommandInvoker {

    protected final Bootstrap bootstrap;

    protected final ConcurrentHashMap<Integer, NettyCommandFuture> requestTable;

    private static Logger log = LoggerFactory.getLogger(AbstractNettyCommandInvoker.class);

    public AbstractNettyCommandInvoker(Bootstrap bootstrap, ConcurrentHashMap<Integer, NettyCommandFuture> requestTable) {
        this.bootstrap = bootstrap;
        this.requestTable = requestTable;
    }

    /**
     * 建立channel
     *
     * @param key
     * @return
     */
    protected abstract Channel createChannel(String key) throws InterruptedException;

    /**
     * 关闭channel
     *
     * @param key
     * @param channel
     */
    protected abstract void closeChannel(String key, Channel channel);

    public NettyCommand invokeSync(String key, NettyCommand command, long timeoutMills) {
        Channel channel = null;
        try {
            channel = createChannel(key);
            if (channel != null && channel.isActive()) {
                NettyCommand repsonse = invokeSync(channel, command, timeoutMills);
                return repsonse;
            } else {
                this.closeChannel(key, channel);
                throw new Exception(key);
            }
        } catch (Exception e) {
            log.debug(e.getMessage(),e);
        }finally {
            this.closeChannel(key, channel);
        }
        return null;
    }

    public void invokeAsync(String key, NettyCommand command, long timeoutMills, NettyCommandCallBack callBack) {
        try {
            Channel channel = createChannel(key);
            if (channel != null && channel.isActive()) {
                invokeAsync(channel, command, timeoutMills, callBack);
            } else {
                this.closeChannel(key, channel);
                throw new Exception(key);
            }
        } catch (Exception e) {
            log.debug(e.getMessage(),e);
        }
    }

    public void invokeOneway(String key, NettyCommand command, long timeoutMills) {
        try {
            Channel channel = createChannel(key);
            if (channel != null && channel.isActive()) {
                invokeOneway(channel, command, timeoutMills);
            } else {
                this.closeChannel(key, channel);
                throw new Exception(key);
            }
        } catch (Exception e) {
            log.debug(e.getMessage(),e);
        }
    }

    /**
     * 异步消息超时重发
     */
    public void scanForReSend() {
        Collection<NettyCommandFuture> values = requestTable.values();

        for (Iterator<NettyCommandFuture> iter = values.iterator(); iter.hasNext(); ) {
            NettyCommandFuture nettyCommandFuture = iter.next();
            if (nettyCommandFuture.isTimeout()) {

                NettyCommand requestCommand = nettyCommandFuture.getRequestCommand();
                iter.remove();
                if (nettyCommandFuture.isReSendRequest()) {
                    try {
                        invokeAsync(nettyCommandFuture.getChannel(), requestCommand, nettyCommandFuture.getTimeoutMills(),
                                nettyCommandFuture.getCallBack());
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                }

            }
        }
    }


    /**
     * 异步递送消息，并保证得到应答
     */
    protected abstract void invokeAsync(final Channel channel, final NettyCommand request, long timeoutMills,
                                        NettyCommandCallBack callBack) throws InterruptedException;

    protected void doInvokeAsync(final Channel channel, final NettyCommand request, final NettyCommandFuture nettyCommandFuture) {
        try {
            requestTable.put(request.getOpaque(), nettyCommandFuture);
            channel.writeAndFlush(request).addListener(new GenericFutureListener<ChannelFuture>() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        nettyCommandFuture.putRequest(request);
                        return;
                    } else {
                        nettyCommandFuture.release();
                        requestTable.remove(request.getOpaque());
                    }
                }
            });
        } catch (Exception e) {
            nettyCommandFuture.release();
            requestTable.remove(request.getOpaque());
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 同步递送消息
     *
     */
    /**
     * 同步递送消息
     */
    public NettyCommand invokeSync(final Channel channel, final NettyCommand request, long timeoutMills)
            throws Exception {
        final NettyCommandFuture nettyCommandFuture = NettyCommandFutureBuilder.createSyncFuture(request.getOpaque(), timeoutMills);
        try {
            log.debug("send request opaque {}",request.getOpaque());
            channel.writeAndFlush(request).addListener(new GenericFutureListener<ChannelFuture>() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        requestTable.put(request.getOpaque(), nettyCommandFuture);
                        nettyCommandFuture.putRequest(request);
                        nettyCommandFuture.setSendRequestOK(true);
                        return;
                    } else {
                        nettyCommandFuture.setSendRequestOK(false);
                        return;
                    }
                }
            });

            log.debug("isSendRequestOK {}",nettyCommandFuture.isSendRequestOK());

            NettyCommand response = null;
            if (nettyCommandFuture.isSendRequestOK()) {
                try {
                    response = nettyCommandFuture.waitResponse(10 * 1000);
                    log.debug("invokeSync response opaque {}",response.getOpaque());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                // 请求发送失败，自然就没有响应
            }
            return response;
        } finally {

        }
    }

    /**
     * 单向递送消息
     */
    protected abstract void invokeOneway(final Channel channel, NettyCommand request, long timeoutMills)
            throws InterruptedException;

}
