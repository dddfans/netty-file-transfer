package org.lqk.netty.forward.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.lqk.netty.forward.protocol.FileInfoResponse;
import org.lqk.netty.forward.protocol.FileSegmentResponse;
import org.lqk.netty.forward.protocol.NettyCommand;
import org.lqk.netty.forward.protocol.NettyCommandBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bert on 16-4-27.
 */
public class NettyRemotingClientHandler  extends SimpleChannelInboundHandler<NettyCommand> {
    private FileResponseProcessorDispatcher fileResponseProcessorDispatcher;

    public NettyRemotingClientHandler(FileResponseProcessorDispatcher fileResponseProcessorDispatcher){
        this.fileResponseProcessorDispatcher = fileResponseProcessorDispatcher;
    }

    private static Logger log = LoggerFactory.getLogger(NettyRemotingClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyCommand msg) throws Exception {
        log.debug("opaque {},cmdCode {},rpctype {},isFlag {}",msg.getOpaque(),msg.getCmdCode(),msg.getType(),msg.isFlag());
        NettyCommandBody body = msg.getBody();
        if (body instanceof FileInfoResponse) {
            FileInfoResponse f = (FileInfoResponse) body;


        }
        if (body instanceof FileSegmentResponse) {
            FileSegmentResponse f = (FileSegmentResponse) body;
            log.debug("position {},blocksize {},filename {},isBlockState {}, isFileState {}", f.getPosition(), f.getBlockSize(),
                    f.getFileName(),f.isBlockState(),f.isFileState());

        }
        fileResponseProcessorDispatcher.processCommand(ctx, msg);
    }
}
