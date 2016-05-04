package org.lqk.netty.forward.protocol.future;

import io.netty.channel.Channel;
import org.lqk.netty.forward.protocol.NettyCommandCallBack;
import org.lqk.netty.forward.util.SemaphoreReleaseOnlyOnce;

/**
 * Created by bert on 16-5-4.
 */
public class SemaphoreNettyCommandFuture extends SimpleNettyCommandFuture {

    private final SemaphoreReleaseOnlyOnce once;

    public SemaphoreNettyCommandFuture(int opaque, long timeoutMills, boolean reSendRequest, SemaphoreReleaseOnlyOnce once, Channel channel, NettyCommandCallBack callBack) {
        super(opaque,timeoutMills,reSendRequest,channel,callBack);
        this.once = once;

    }


    @Override
    public void release() {
        this.once.release();
    }
}
