package org.lqk.netty.forward.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.lqk.netty.forward.NettyCommandInvoker;
import org.lqk.netty.forward.RemotingClient;
import org.lqk.netty.forward.client.invoker.PooledNettyCommandInvoker;
import org.lqk.netty.forward.client.invoker.SimpleNettyCommandInvoker;
import org.lqk.netty.forward.protocol.NettyCommand;
import org.lqk.netty.forward.protocol.NettyCommandCallBack;
import org.lqk.netty.forward.protocol.future.NettyCommandFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2015/8/25.
 */
public class NettyRemotingClient implements RemotingClient, NettyCommandInvoker {

    private final Bootstrap bootstrap;

    private final EventLoopGroup eventWorkerLoopGroup;

    private final ConcurrentHashMap<Integer, NettyCommandFuture> requestTable = new ConcurrentHashMap<Integer, NettyCommandFuture>(
            256);

    private NettyCommandInvoker nettyCommandInvoker;
    // 定时器
    private final Timer timer = new Timer("ClientHouseKeepingService", true);
    private FileResponseProcessorDispatcher fileResponseProcessorDispatcher;

    private static Logger log = LoggerFactory.getLogger(NettyRemotingClient.class);

    public NettyRemotingClient() {
        this.bootstrap = new Bootstrap();
        this.eventWorkerLoopGroup = new NioEventLoopGroup();
        fileResponseProcessorDispatcher = new FileResponseProcessorDispatcher(requestTable);
//        nettyCommandInvoker = new SimpleNettyCommandInvoker(bootstrap,requestTable);
        nettyCommandInvoker = new PooledNettyCommandInvoker(bootstrap,requestTable,(long)(10 * 1000),new ClientChannelInitializer(fileResponseProcessorDispatcher));
    }


    public void start() {
        this.bootstrap
                .group(eventWorkerLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ClientChannelInitializer(fileResponseProcessorDispatcher));

//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                nettyCommandInvoker.scanForReSend();
//            }
//        }, 5 * 1000, 1000);
    }



    public void stop() {
        this.eventWorkerLoopGroup.shutdownGracefully();
    }


    public NettyCommand invokeSync(String addr, NettyCommand command, long timeoutMills) {
        return nettyCommandInvoker.invokeSync(addr,command,timeoutMills);
    }

    public void invokeAsync(String addr, NettyCommand command, long timeoutMills, NettyCommandCallBack callBack) {
        nettyCommandInvoker.invokeAsync(addr,command,timeoutMills,callBack);
    }

    public void invokeOneway(String addr, NettyCommand command, long timeoutMills) {
        nettyCommandInvoker.invokeOneway(addr,command,timeoutMills);
    }
}
