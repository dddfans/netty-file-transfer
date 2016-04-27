package org.lqk.netty.forward.framework;

import io.netty.channel.ChannelHandlerContext;
import org.lqk.netty.forward.file.FileStorage;
import org.lqk.netty.forward.processor.FileInfoRequestProcessor;
import org.lqk.netty.forward.processor.FileRequestProcessor;
import org.lqk.netty.forward.processor.FileSegmentRequestProcessor;
import org.lqk.netty.forward.protocol.NettyCommand;
import org.lqk.netty.forward.protocol.NettyCommandCode;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2015/8/26.
 */
public class FileRequestProcessorDispatcher {
    private ConcurrentHashMap<String, FileStorage> fileTable = new ConcurrentHashMap<String, FileStorage>();
    private final HashMap<Integer, FileRequestProcessor> processorTable = new HashMap<Integer, FileRequestProcessor>();
    private final String baseDir;

    public FileRequestProcessorDispatcher(String baseDir) {
        this.baseDir = baseDir;
//		ExecutorService es = Executors.newCachedThreadPool();
        // this.registProcessor(1, new DefaultProcessor(), es);
//		FileInfoRequestProcessor processor = new FileInfoRequestProcessor(baseDir);
        FileRequestProcessor fileInfoRequestProcessor = new FileInfoRequestProcessor(fileTable, baseDir, NettyCommandCode.FILE_INFO.getCode());
        FileRequestProcessor fileSegmentRequestProcessor = new FileSegmentRequestProcessor(fileTable, baseDir, NettyCommandCode.FILE_SEGMENT.getCode());
        processorTable.put(fileInfoRequestProcessor.getCmdCode(), fileInfoRequestProcessor);
        processorTable.put(fileSegmentRequestProcessor.getCmdCode(), fileSegmentRequestProcessor);
    }

    /**
     * 处理请求消息
     *
     * @param ctx
     * @param command
     */
    public void processCommand(final ChannelHandlerContext ctx, final NettyCommand command) {

        final FileRequestProcessor processor = processorTable.get(command.getCmdCode());

        if (processor != null) {

            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        // 拿到处理器进行处理
                        NettyCommand response = processor.process(command);

                        if (!command.isRpcOneway()) {
                            if (response != null) {
                                response.setOpaque(command.getOpaque());
                                ctx.writeAndFlush(response);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };


            ctx.executor().submit(runnable);

        } else {
            // TODO无法处理的请求
        }
    }
}
