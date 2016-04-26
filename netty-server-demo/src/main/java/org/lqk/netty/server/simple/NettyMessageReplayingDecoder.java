package org.lqk.netty.server.simple;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.lqk.netty.codec.marshalling.MarshallingDecoder;
import org.lqk.netty.protocol.Header;
import org.lqk.netty.protocol.NettyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * 简单实现读取文件数据并写入到磁盘上
 * Created by bert on 16-4-26.
 */
public class NettyMessageReplayingDecoder extends ReplayingDecoder<NettyMessageReplayingDecoder.State> {

    private NettyMessage nettyMessage;
    private MarshallingDecoder marshallingDecoder;

    private int readedBodyLength = 0;

    private static Logger log = LoggerFactory.getLogger(NettyMessageReplayingDecoder.class);

    public NettyMessageReplayingDecoder() throws IOException {
        // 设置初始化状态
        super(State.HEADER);
        this.marshallingDecoder = new MarshallingDecoder();

    }

    enum State {
        HEADER,
        BODY
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case HEADER: {
                Header header = new Header();
                Header.readHeader(header, in, marshallingDecoder);
                this.nettyMessage = new NettyMessage();
                nettyMessage.setHeader(header);
                checkpoint(State.BODY);
                return;
            }
            case BODY: {
                NettyMessage.readBody(nettyMessage, in);
                out.add(nettyMessage);
                // 恢复初始状态
                reset();
                return;
            }
            default: {
                log.error("error state");
            }


        }
    }

    private void reset() {
        this.nettyMessage = null;
        checkpoint(State.HEADER);
    }

    @Override
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Handle the last unfinished message.
        if (nettyMessage != null) {
            decode(ctx, in, out);
            // 算是最后的数据，解析不成功，则重置
            if (nettyMessage != null) {
                reset();
            }
        }

    }
}
