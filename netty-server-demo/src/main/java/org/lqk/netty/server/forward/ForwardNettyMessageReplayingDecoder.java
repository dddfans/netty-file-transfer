package org.lqk.netty.server.forward;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.lqk.netty.forward.protocol.*;
import org.lqk.netty.protocol.NettyMessageType;
import org.lqk.netty.codec.marshalling.MarshallingDecoder;
import org.lqk.netty.forward.Constant;
import org.lqk.netty.protocol.NettyMessageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 发送文件前，先发送一个文件信息，然后再发送文件数据
 * Created by bert on 16-4-26.
 */
public class ForwardNettyMessageReplayingDecoder extends ReplayingDecoder<ForwardNettyMessageReplayingDecoder.State> {
    private NettyMessageHeader nettyMessageHeader;
    private long position = 0;
    private int opaque;
    private byte[] b = new byte[Constant.BLOCK_SIZE];

    private String baseDir;
    private MarshallingDecoder marshallingDecoder;


    private static Logger log = LoggerFactory.getLogger(ForwardNettyMessageReplayingDecoder.class);

    public ForwardNettyMessageReplayingDecoder(String baseDir) throws IOException {
        // 设置初始化状态
        super(State.HEADER);
        this.baseDir = baseDir;
        this.marshallingDecoder = new MarshallingDecoder();

    }

    enum State {
        HEADER,
        FILE_INFO_BODY,
        FILE_DATA_BODY
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case HEADER: {
                this.nettyMessageHeader = new NettyMessageHeader();
                NettyMessageHeader.readHeader(nettyMessageHeader, in, marshallingDecoder);
                State nextState = readState(nettyMessageHeader);
                checkpoint(nextState);
                return;
            }
            case FILE_INFO_BODY: {
                this.opaque = NettyCommand.autoIncrement();
                out.add(fileInfoNettyCommand(opaque));
                reset();
                return;
            }
            case FILE_DATA_BODY: {
                boolean done = false;
                // 确认本次应读取的数据长度,readableBytes不是实际的值
//                int len = in.readableBytes();
                int expectedLen = nettyMessageHeader.getBodyLength();
                // 最多读取Constant.BLOCK_SIZE大小
                int len = (int)(expectedLen - position);
                if (len <= Constant.BLOCK_SIZE){
                    done = true;
                }else{
                    len = Constant.BLOCK_SIZE;
                }
//                log.debug("receive {} bytes 1, expectedLen {}", len, expectedLen);
                in.readBytes(b,0,len);
                // 构造NettyCommand对象
                if (done) {
                    out.add(fileSegmentNettyCommand(b,0,len,opaque,true));
                    reset();
                } else {
                    out.add(fileSegmentNettyCommand(b,0,len,opaque,false));
                    position += len;
                    checkpoint();
                }
                return;
            }
            default: {
                log.error("error state");
            }


        }
    }

    private NettyCommand fileInfoNettyCommand(int opaque) {
        NettyCommand command = new NettyCommand(NettyCommandCode.FILE_INFO.getCode(), NettyCommandType.File_Request.getCode(), false, opaque);
        FileInfoRequest request = new FileInfoRequest();
        Map<String, Object> attachment = nettyMessageHeader.getAttachment();
        request.setFileName((String) attachment.get("fileName"));
        request.setBlockSize(Constant.BLOCK_SIZE);
        request.setFileSize((Long) attachment.get("fileSize"));
        request.setMd5((String) attachment.get("md5"));
        command.setBody(request);
        return command;

    }

    private NettyCommand fileSegmentNettyCommand(byte[] b,int offset,int len, int opaque,boolean isLast) {
        NettyCommandCode nettyCommandCode = isLast ? NettyCommandCode.LAST_FILE_SEGMENT : NettyCommandCode.FILE_SEGMENT;
        NettyCommand command = new NettyCommand(nettyCommandCode.getCode(), NettyCommandType.File_Request.getCode(), false, opaque);
        FileSegmentRequest request = new FileSegmentRequest();
        request.setBlockSize(len);
        request.setContent(Arrays.copyOfRange(b,offset,offset + len));
        request.setPosition(position);
        request.setFileName((String) nettyMessageHeader.getAttachment().get("fileName"));
        request.setContent(b);
        command.setBody(request);
        return command;
    }


    private State readState(NettyMessageHeader header) {
        byte type = header.getType();
        NettyMessageType messageType = NettyMessageType.valueOf(type);
        if (null == messageType) {
            return null;
        }
        switch (messageType) {
            case FILE_INFO_REQ:
                return State.FILE_INFO_BODY;
            case FILE_DATA_REQ:
                return State.FILE_DATA_BODY;
            default:
                log.error("error type");
        }
        return null;
    }

    private void reset() {
        this.nettyMessageHeader = null;
        this.opaque = 0;
        this.position = 0;
        checkpoint(State.HEADER);
    }

    @Override
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Handle the last unfinished message.
        if (nettyMessageHeader != null) {
            decode(ctx, in, out);
            // 算是最后的数据，解析不成功，则重置
            if (nettyMessageHeader != null) {
                reset();
            }
        }
    }
}
