package org.lqk.netty.server.complex;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.lqk.netty.protocol.NettyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by bert on 16-4-26.
 */
public class ComplexNettyMessageHandler extends ChannelHandlerAdapter{
    private static Logger log = LoggerFactory.getLogger(ComplexNettyMessageHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        log.debug("receive message, type {}",message.getHeader().getType());
    }
}
