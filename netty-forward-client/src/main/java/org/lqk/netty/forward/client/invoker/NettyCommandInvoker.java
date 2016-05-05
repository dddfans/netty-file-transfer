package org.lqk.netty.forward.client.invoker;

import org.lqk.netty.forward.protocol.NettyCommand;
import org.lqk.netty.forward.protocol.NettyCommandCallBack;

/**
 * Created by bert on 16-4-27.
 */
public interface NettyCommandInvoker extends  Cloneable{
    NettyCommand invokeSync(String key, NettyCommand command, long timeoutMills);
    void invokeAsync(String key, NettyCommand command, long timeoutMills, NettyCommandCallBack callBack);
    void invokeOneway(String key, NettyCommand command, long timeoutMills);
}
