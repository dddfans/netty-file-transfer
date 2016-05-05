package org.lqk.netty.forward;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.lqk.netty.forward.server.framework.FileRequestProcessorDispatcher;
import org.lqk.netty.forward.protocol.NettyCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bert on 16-4-27.
 */
public class NettyCommandServerHandler extends SimpleChannelInboundHandler<NettyCommand> {

    private FileRequestProcessorDispatcher fileRequestProcessorDispatcher;

    private static Logger log = LoggerFactory.getLogger(NettyCommandServerHandler.class);

    public NettyCommandServerHandler(FileRequestProcessorDispatcher fileRequestProcessorDispatcher) {
        this.fileRequestProcessorDispatcher = fileRequestProcessorDispatcher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("connect established");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("connect abort");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyCommand msg) throws Exception {
        fileRequestProcessorDispatcher.processCommand(ctx, msg);
    }
}
