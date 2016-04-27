package org.lqk.netty.forward.protocol;

import org.lqk.netty.forward.protocol.NettyCommandFuture;

/**
 * Created by Administrator on 2015/9/1.
 */
public interface NettyCommandCallBack {

    void executeCallBack(final NettyCommandFuture requestFuture);

}
