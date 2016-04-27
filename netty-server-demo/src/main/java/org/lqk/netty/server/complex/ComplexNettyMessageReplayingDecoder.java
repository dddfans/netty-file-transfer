package org.lqk.netty.server.complex;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.apache.commons.io.IOUtils;
import org.lqk.netty.protocol.NettyMessageType;
import org.lqk.netty.codec.marshalling.MarshallingDecoder;
import org.lqk.netty.protocol.NettyMessageHeader;
import org.lqk.netty.protocol.NettyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * 发送文件前，先发送一个文件信息，然后再发送文件数据
 * Created by bert on 16-4-26.
 */
public class ComplexNettyMessageReplayingDecoder extends ReplayingDecoder<ComplexNettyMessageReplayingDecoder.State> {
    private NettyMessage nettyMessage;
    private FileChannel channel;
    private long position = 0;
    private String baseDir;
    private MarshallingDecoder marshallingDecoder;
    private final static int BLOCK_SIZE = 1024 * 1024;

    private static Logger log = LoggerFactory.getLogger(ComplexNettyMessageReplayingDecoder.class);

    public ComplexNettyMessageReplayingDecoder(String baseDir) throws IOException {
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
                NettyMessageHeader header = new NettyMessageHeader();
                NettyMessageHeader.readHeader(header, in, marshallingDecoder);
                this.nettyMessage = new NettyMessage();
                nettyMessage.setHeader(header);
                State nextState = readState(header);
                if(State.FILE_DATA_BODY == nextState){
                    String filePath = baseDir + File.separator + header.getAttachment().get("fileName");
                    RandomAccessFile file = new RandomAccessFile(filePath,"rw");
                    this.channel = file.getChannel();
                }
                checkpoint(nextState);
                return;
            }
            case FILE_INFO_BODY: {
                NettyMessage.readBody(nettyMessage,in);
                out.add(nettyMessage);
                reset();
                return;
            }
            case FILE_DATA_BODY: {
                boolean done = false;
//                int len = in.readableBytes();

                int expectedLen = nettyMessage.getHeader().getBodyLength();
                int len = (int)(expectedLen - position);
                if (len <= BLOCK_SIZE){
                    done = true;
                }else{
                    len = BLOCK_SIZE;
                }
                channel.position(position);
                byte[] b = new byte[len];
                log.debug("receive {} bytes 2, len {}",in.readableBytes(),len);
                in.readBytes(b);
                // 此处可以作为实例变量，减少重复创建
                ByteBuffer byteBuffer = ByteBuffer.allocate(len);
                byteBuffer.put(b);
                byteBuffer.flip();
                while(byteBuffer.hasRemaining()){
                    channel.write(byteBuffer);
                }
                if(done){
                    reset();
                }else{
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

    private State readState(NettyMessageHeader header){
        byte type = header.getType();
        NettyMessageType messageType = NettyMessageType.valueOf(type);
        if(null == messageType){
            return null;
        }
        switch (messageType){
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
        this.nettyMessage = null;
        IOUtils.closeQuietly(this.channel);
        this.position = 0;
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
