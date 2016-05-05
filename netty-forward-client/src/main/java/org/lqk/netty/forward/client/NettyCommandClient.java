package org.lqk.netty.forward.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.lqk.netty.forward.RemotingClient;
import org.lqk.netty.forward.client.invoker.NettyCommandInvoker;
import org.lqk.netty.forward.client.invoker.NettyCommandInvokerBuilder;
import org.lqk.netty.forward.protocol.NettyCommand;
import org.lqk.netty.forward.protocol.NettyCommandCallBack;
import org.lqk.netty.forward.protocol.future.NettyCommandFuture;
import org.lqk.netty.zk.client.ServerAddressProvider;
import org.lqk.netty.zk.client.ZookeeperServerAddressProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2015/8/25.
 */
public class NettyCommandClient implements RemotingClient, NettyCommandInvoker {

    private final Bootstrap bootstrap;

    private final EventLoopGroup eventWorkerLoopGroup;

    private final ConcurrentHashMap<Integer, NettyCommandFuture> requestTable = new ConcurrentHashMap<Integer, NettyCommandFuture>(
            256);

    private NettyCommandInvoker nettyCommandInvoker;
    // 定时器
    private final Timer timer = new Timer("ClientHouseKeepingService", true);
    private FileResponseProcessorDispatcher fileResponseProcessorDispatcher;

    private ServerAddressProvider serverAddressProvider;

    private static Logger log = LoggerFactory.getLogger(NettyCommandClient.class);

    public NettyCommandClient() throws Exception {
        this.bootstrap = new Bootstrap();
        this.eventWorkerLoopGroup = new NioEventLoopGroup();
        fileResponseProcessorDispatcher = new FileResponseProcessorDispatcher(requestTable);
//        nettyCommandInvoker = new SimpleNettyCommandInvoker(bootstrap,requestTable);
//        nettyCommandInvoker = NettyCommandInvokerBuilder.create(bootstrap, requestTable, new NettyCommandClientChannelInitializer(fileResponseProcessorDispatcher),
//                (long) (10 * 1000), 1, 1);
        serverAddressProvider = new ZookeeperServerAddressProvider();
        nettyCommandInvoker = NettyCommandInvokerBuilder.create(bootstrap, requestTable, new NettyCommandClientChannelInitializer(fileResponseProcessorDispatcher),
                (long) (10 * 1000), 1, 1,serverAddressProvider);
    }


    public void start() {
        this.bootstrap
                .group(eventWorkerLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyCommandClientChannelInitializer(fileResponseProcessorDispatcher));

//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                nettyCommandInvoker.scanForReSend();
//            }
//        }, 5 * 1000, 1000);
    }



    public void stop() throws IOException {
        this.eventWorkerLoopGroup.shutdownGracefully();
        serverAddressProvider.close();
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
