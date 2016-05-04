package org.lqk.netty.forward.protocol.future;

import io.netty.channel.Channel;
import org.lqk.netty.forward.protocol.NettyCommand;
import org.lqk.netty.forward.protocol.NettyCommandCallBack;
import org.lqk.netty.forward.util.SemaphoreReleaseOnlyOnce;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2015/8/26.
 */
public class SimpleNettyCommandFuture implements NettyCommandFuture{

    private volatile NettyCommand requestCommand;

    private volatile NettyCommand responseCommand;

    private boolean sendRequestOK = true;

    private NettyCommandCallBack callBack;

    private final int opaque;

    private final long timeoutMills;

    private long beginTimestamp = System.currentTimeMillis();

    private CountDownLatch latch = new CountDownLatch(1);

    private final boolean reSendRequest;

    private Channel channel;

    public SimpleNettyCommandFuture(int opaque, long timeoutMills, boolean reSendRequest, Channel channel, NettyCommandCallBack callBack) {
        this.opaque = opaque;
        this.timeoutMills = timeoutMills;
        this.reSendRequest = reSendRequest;
        this.channel = channel;
        this.callBack = callBack;
    }

    public void putRequest(NettyCommand command) {
        this.requestCommand = command;
    }

    public void putResponse(NettyCommand command) {
        this.responseCommand = command;
        this.latch.countDown();
    }

    public void release(){}

    public NettyCommand waitResponse(long timeoutMills) throws Exception {
        this.latch.await(timeoutMills, TimeUnit.MILLISECONDS);
        return this.responseCommand;
    }

    public void executeCallBack() {
        if (this.callBack != null) {
            this.callBack.executeCallBack(this);
        }
    }

    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }

    public boolean isSendRequestOK() {
        return sendRequestOK;
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() - beginTimestamp > timeoutMills;
    }

    public NettyCommand getResponseCommand() {
        return responseCommand;
    }

    public NettyCommand getRequestCommand() {
        return requestCommand;
    }

    public boolean isReSendRequest() {
        return reSendRequest;
    }

    public Channel getChannel() {
        return channel;
    }

    public long getTimeoutMills() {
        return timeoutMills;
    }

    public NettyCommandCallBack getCallBack() {
        return callBack;
    }

    public void setCallBack(NettyCommandCallBack callBack) {
        this.callBack = callBack;
    }


}
