package org.lqk.netty.server.sectional;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.apache.commons.io.IOUtils;
import org.lqk.netty.codec.marshalling.MarshallingDecoder;
import org.lqk.netty.protocol.Header;
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
 * 如果文件较大时，写入文件前不可能将所有数据都缓冲到内存中，因此需要边接收，边向磁盘写入数据
 * Created by bert on 16-4-26.
 */
public class NettyMessageSectionalReplayingDecoder extends ReplayingDecoder<NettyMessageSectionalReplayingDecoder.State> {
    private NettyMessage nettyMessage;
    private FileChannel channel;
    private long position = 0;
    private String baseDir;
    private MarshallingDecoder marshallingDecoder;


    private static Logger log = LoggerFactory.getLogger(NettyMessageSectionalReplayingDecoder.class);

    public NettyMessageSectionalReplayingDecoder(String baseDir) throws IOException {
        // 设置初始化状态
        super(State.HEADER);
        this.baseDir = baseDir;
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
                String filePath = baseDir + File.separator + header.getAttachment().get("fileName");
                RandomAccessFile file = new RandomAccessFile(filePath,"rw");
                this.channel = file.getChannel();
                checkpoint(State.BODY);
                return;
            }
            case BODY: {
                boolean done = false;
                int len = in.readableBytes();
                log.debug("receive {} bytes",len);
                int expectedLen = nettyMessage.getHeader().getBodyLength();
                if(position + len  >= expectedLen){
                    len = (int)(expectedLen - position);
                    done = true;
                }
                channel.position(position);
                byte[] b = new byte[len];
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
