package org.lqk.netty.forward.protocol.future;

import io.netty.channel.Channel;
import org.lqk.netty.forward.protocol.NettyCommandCallBack;
import org.lqk.netty.forward.util.SemaphoreReleaseOnlyOnce;

/**
 * Created by Administrator on 2015/8/26.
 */
public class NettyCommandFutureBuilder {

    public static NettyCommandFuture createSyncFuture(int opaque, long timeoutMills) {
        return new SimpleNettyCommandFuture(opaque, timeoutMills, false, null, null);
    }

    public static NettyCommandFuture createAsynFuture(int opaque, long timeoutMills, boolean reSendRequest, SemaphoreReleaseOnlyOnce once, Channel channel) {
        return new SemaphoreNettyCommandFuture(opaque, timeoutMills, reSendRequest, once, channel, null);
    }

    public static NettyCommandFuture createAsynFuture(int opaque, long timeoutMills, boolean reSendRequest, SemaphoreReleaseOnlyOnce once, Channel channel, NettyCommandCallBack callBack) {
        return new SemaphoreNettyCommandFuture(opaque, timeoutMills, reSendRequest, once, channel, callBack);
    }

}
