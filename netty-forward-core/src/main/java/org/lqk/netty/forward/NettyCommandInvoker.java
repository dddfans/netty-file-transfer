package org.lqk.netty.forward;

import org.lqk.netty.forward.protocol.NettyCommand;
import org.lqk.netty.forward.protocol.NettyCommandCallBack;

/**
 * Created by bert on 16-4-27.
 */
public interface NettyCommandInvoker {
    NettyCommand invokeSync(String addr, NettyCommand command, long timeoutMills);
    void invokeAsync(String addr, NettyCommand command, long timeoutMills, NettyCommandCallBack callBack);
    void invokeOneway(String addr, NettyCommand command, long timeoutMills);
}
