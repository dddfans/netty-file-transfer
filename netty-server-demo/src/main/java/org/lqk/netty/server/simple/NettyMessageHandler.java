package org.lqk.netty.server.simple;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.lqk.netty.protocol.NettyMessageHeader;
import org.lqk.netty.protocol.NettyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by bert on 16-4-26.
 */
public class NettyMessageHandler extends ChannelInboundHandlerAdapter {
    private String baseDir;
    private static Logger log = LoggerFactory.getLogger(NettyMessageHandler.class);

    public NettyMessageHandler(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("receive message");
        final NettyMessage message = (NettyMessage) msg;

        ctx.executor().submit(new Runnable() {
            public void run() {
                NettyMessageHeader header = message.getHeader();
                String fileName = (String) header.getAttachment().get("fileName");
                if (StringUtils.isNotEmpty(fileName)) {
                    try {
                        FileUtils.writeByteArrayToFile(new File(baseDir + File.separator + fileName), message.getBody());
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }

            }
        });
    }
}
