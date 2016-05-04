package org.lqk.netty.forward.server.framework;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.lqk.netty.forward.protocol.*;
import org.lqk.netty.forward.server.file.FileStorage;
import org.lqk.netty.forward.server.processor.FileInfoRequestProcessor;
import org.lqk.netty.forward.server.processor.FileRequestProcessor;
import org.lqk.netty.forward.server.processor.FileSegmentRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2015/8/26.
 */
public class FileRequestProcessorDispatcher {
    private ConcurrentHashMap<String, FileStorage> fileTable = new ConcurrentHashMap<String, FileStorage>();
    private final HashMap<Integer, FileRequestProcessor> processorTable = new HashMap<Integer, FileRequestProcessor>();
    private final String baseDir;

    private static Logger log = LoggerFactory.getLogger(FileRequestProcessorDispatcher.class);

    public FileRequestProcessorDispatcher(String baseDir) {
        this.baseDir = baseDir;
//		ExecutorService es = Executors.newCachedThreadPool();
        // this.registProcessor(1, new DefaultProcessor(), es);
//		FileInfoRequestProcessor processor = new FileInfoRequestProcessor(baseDir);
        FileRequestProcessor fileInfoRequestProcessor = new FileInfoRequestProcessor(fileTable, baseDir, new int[]{NettyCommandCode.FILE_INFO.getCode()});
        FileRequestProcessor fileSegmentRequestProcessor = new FileSegmentRequestProcessor(fileTable, baseDir,
                new int[]{NettyCommandCode.FILE_SEGMENT.getCode(), NettyCommandCode.LAST_FILE_SEGMENT.getCode()});
        int[] cmdCodes = fileInfoRequestProcessor.getCmdCodes();
        for(int cmdCode : cmdCodes){
            processorTable.put(cmdCode, fileInfoRequestProcessor);
        }
        cmdCodes = fileSegmentRequestProcessor.getCmdCodes();
        for(int cmdCode : cmdCodes){
            processorTable.put(cmdCode, fileSegmentRequestProcessor);
        }
    }

    /**
     * 处理请求消息
     *
     * @param ctx
     * @param command
     */
    public void processCommand(final ChannelHandlerContext ctx, final NettyCommand command) {

        log.debug("receive opaque {},cmdCode {},rpctype {}", command.getOpaque(), command.getCmdCode(), command.getType());
        NettyCommandBody body = command.getBody();
        if (body instanceof FileInfoRequest) {
            FileInfoRequest f = (FileInfoRequest) body;
            log.debug("receive fileName {},fileSize {},md5 {}", f.getFileName(), f.getFileSize(),
                    f.getMd5());

        }
        if (body instanceof FileSegmentRequest) {
            FileSegmentRequest f = (FileSegmentRequest) body;
            log.debug("receive position {},blocksize {},filename {},crc32 {}", f.getPosition(), f.getBlockSize(),
                    f.getFileName(), f.getCrc32());

        }

        final FileRequestProcessor processor = processorTable.get(command.getCmdCode());

        if (processor != null) {
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        // 拿到处理器进行处理
                        NettyCommand response = processor.process(command);
                        log.debug("send content {}", JSON.toJSONString(response));
                        if (!command.isRpcOneway()) {
                            if (response != null) {
                                response.setOpaque(command.getOpaque());
                                ctx.writeAndFlush(response);
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            };


            ctx.executor().submit(runnable);

        } else {
            // TODO无法处理的请求
        }
    }
}
