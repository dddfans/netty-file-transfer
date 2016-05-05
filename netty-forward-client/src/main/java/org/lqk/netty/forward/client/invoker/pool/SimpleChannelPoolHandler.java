package org.lqk.netty.forward.client.invoker.pool;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.pool.ChannelPoolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bert on 16-5-5.
 */
public class SimpleChannelPoolHandler implements ChannelPoolHandler {

    private ChannelInitializer channelInitializer;

    private static Logger log = LoggerFactory.getLogger(SimpleChannelPoolHandler.class);

    public SimpleChannelPoolHandler(ChannelInitializer channelInitializer){
        this.channelInitializer = channelInitializer;
    }

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
