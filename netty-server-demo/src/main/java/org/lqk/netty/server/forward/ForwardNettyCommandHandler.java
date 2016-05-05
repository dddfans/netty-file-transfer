package org.lqk.netty.server.forward;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.lqk.netty.forward.client.NettyCommandClient;
import org.lqk.netty.forward.protocol.*;
import org.lqk.netty.protocol.NettyMessage;
import org.lqk.netty.protocol.NettyMessageHeader;
import org.lqk.netty.protocol.NettyMessageType;
import org.lqk.netty.zk.ServerServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;


/**
 * Created by bert on 16-4-26.
 */
public class ForwardNettyCommandHandler extends SimpleChannelInboundHandler<NettyCommand> {
    private NettyCommandClient nettyRemotingClient;
    private CRC32 crc32 = new CRC32();
    private static Logger log = LoggerFactory.getLogger(ForwardNettyCommandHandler.class);

    public ForwardNettyCommandHandler(NettyCommandClient nettyRemotingClient) {
        this.nettyRemotingClient = nettyRemotingClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyCommand nettyCommand) throws Exception {
        int code = nettyCommand.getCmdCode();
        NettyCommandCode commandCode = NettyCommandCode.valueOf(code);
        log.debug("receive command, code {}", commandCode.name());
        String key = nettyCommand.getOpaque() + ":" + ServerServiceType.AUDIO.getCode();
        //TODO 将其改为异步操作
        switch (commandCode) {
            case FILE_INFO: {
//                NettyCommand responseCommand = nettyRemotingClient.invokeSync("127.0.0.1:8000", nettyCommand, 10 * 1000);
                NettyCommand responseCommand = nettyRemotingClient.invokeSync(key, nettyCommand, 10 * 1000);
                log.debug("opaque {}, cmdCode {},type {}", responseCommand.getOpaque(), responseCommand.getCmdCode(), responseCommand.getType());
                FileInfoResponse f = (FileInfoResponse) responseCommand.getBody();
                if (null != f) {
                    log.debug("position {},blockSize {},filename {} {}, isFileState {}", f.getPosition(), f.getBlockSize(),
                            f.getFileName(), f.getFileState());
                }
                NettyMessage message = new NettyMessage();
                NettyMessageHeader header = new NettyMessageHeader();
                message.setHeader(header);
                header.setType(NettyMessageType.FILE_INFO_RESP.value());
                Map<String, Object> attachment = new HashMap<String, Object>();
                header.setAttachment(attachment);
                attachment.put("position", f.getPosition());
                attachment.put("blockSize", f.getBlockSize());
                attachment.put("fileName", f.getFileName());
                attachment.put("fileState", f.getFileState());
                ctx.writeAndFlush(attachment);
                return;
            }
            case FILE_SEGMENT: {
                FileSegmentRequest request = (FileSegmentRequest) nettyCommand.getBody();
                crc32.reset();
                crc32.update(request.getContent());
                request.setCrc32(crc32.getValue());
                NettyCommand responseCommand = nettyRemotingClient.invokeSync(key, nettyCommand, 10 * 1000);
                log.debug("opaque {}, cmdCode {},type {}", responseCommand.getOpaque(), responseCommand.getCmdCode(), responseCommand.getType());
                FileSegmentResponse f = (FileSegmentResponse) responseCommand.getBody();
                if (null != f) {
                    log.debug("position {},blockSize {},filename {},isBlockState {}, isFileState {}", f.getPosition(), f.getBlockSize(),
                            f.getFileName(), f.isBlockState(), f.isFileState());
                    if(! f.isBlockState()){
                        // 如果某个块处理失败，则认为整个块失败，还是其它策略
                        // 转发的角色，是一切逻辑都不管么，是否需要保存中间状态
                        // 从断点续传 到 分段传输的转换，还是有很多东西要注意的
                    }
                }
                return;
            }
            case LAST_FILE_SEGMENT: {
                FileSegmentRequest request = (FileSegmentRequest) nettyCommand.getBody();
                crc32.reset();
                crc32.update(request.getContent());
                request.setCrc32(crc32.getValue());
                NettyCommand responseCommand = nettyRemotingClient.invokeSync(key, nettyCommand, 10 * 1000);
                log.debug("opaque {}, cmdCode {},type {}", responseCommand.getOpaque(), responseCommand.getCmdCode(), responseCommand.getType());
                FileSegmentResponse f = (FileSegmentResponse) responseCommand.getBody();
                if (null != f) {
                    log.debug("position {},blockSize {},filename {},isBlockState {}, isFileState {}", f.getPosition(), f.getBlockSize(),
                            f.getFileName(), f.isBlockState(), f.isFileState());
                    NettyMessage message = new NettyMessage();
                    NettyMessageHeader header = new NettyMessageHeader();
                    message.setHeader(header);
                    header.setType(NettyMessageType.FILE_DATA_RESP.value());
                    Map<String, Object> attachment = new HashMap<String, Object>();
                    header.setAttachment(attachment);
                    attachment.put("position", f.getPosition());
                    attachment.put("blockSize", f.getBlockSize());
                    attachment.put("fileName", f.getFileName());
                    attachment.put("isFinished", f.isFileState());
                    ctx.writeAndFlush(attachment);
                }
                return;
            }

            default: {
                log.error("error message type");
            }
        }
    }
}
