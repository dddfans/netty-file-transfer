package org.lqk.netty.forward.protocol.future;

import io.netty.channel.Channel;
import org.lqk.netty.forward.protocol.NettyCommand;
import org.lqk.netty.forward.protocol.NettyCommandCallBack;

/**
 * Created by Administrator on 2015/8/26.
 */
public interface NettyCommandFuture {

    void putRequest(NettyCommand command);

    void putResponse(NettyCommand command);

    void release() ;

    NettyCommand waitResponse(long timeoutMills) throws Exception ;

    void executeCallBack();

    void setSendRequestOK(boolean sendRequestOK);

    boolean isSendRequestOK();

    boolean isTimeout();

    NettyCommand getRequestCommand();

    boolean isReSendRequest();

    Channel getChannel();

    long getTimeoutMills();

     NettyCommandCallBack getCallBack();
}
