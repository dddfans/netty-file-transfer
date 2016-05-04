package org.lqk.netty.forward.client;

import io.netty.channel.ChannelHandlerContext;
import org.lqk.netty.forward.protocol.NettyCommand;
import org.lqk.netty.forward.protocol.future.NettyCommandFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2015/8/26.
 */
public class FileResponseProcessorDispatcher {
    private ConcurrentHashMap<Integer, NettyCommandFuture> requestTable;
    private final static Logger log = LoggerFactory.getLogger(FileResponseProcessorDispatcher.class);

    public FileResponseProcessorDispatcher(ConcurrentHashMap<Integer, NettyCommandFuture> requestTable) {
        this.requestTable = requestTable;

    }

    /**
     * 处理请求消息
     *
     * @param ctx
     * @param command
     */
    public void processCommand(final ChannelHandlerContext ctx, final NettyCommand command) {
        log.debug("request table size {}", requestTable.size());
        final NettyCommandFuture requestFuture = requestTable.get(command.getOpaque());
        /*
		 *  保存最后返回的结果，根据返回结果的类型和相关属性值，可以知道是否发送成功了。
		 *  做的更好些，为callback添加一个方法，就可以计算进度
		 */
        requestFuture.putResponse(command);

        if (requestFuture.getCallBack() != null) {

            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        requestFuture.executeCallBack();
                    } catch (Throwable e) {
                        log.error(e.getMessage(), e);
                    }
                    requestTable.remove(command.getOpaque());
                }
            };

            ctx.executor().submit(runnable);
        }

    }
}
