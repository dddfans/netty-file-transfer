package org.lqk.netty.server.forward;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.lqk.netty.NettyConstant;
import org.lqk.netty.server.NettyMessageEncoder;
import org.lqk.netty.server.forward.client.NettyRemotingClient;

import java.io.IOException;

/**
 * Created by bert on 16-4-26.
 */
public class ForwardNettyServer {
    private NettyRemotingClient nettyRemotingClient;
    public ForwardNettyServer(NettyRemotingClient nettyRemotingClient){
        this.nettyRemotingClient = nettyRemotingClient;
    }
    public void bind() throws Exception {
        // 配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)
                            throws IOException {
                        ch.pipeline().addLast(
                                new ForwardNettyMessageReplayingDecoder("/home/bert/tmp1"));
                        ch.pipeline().addLast(new NettyMessageEncoder());
                        ch.pipeline().addLast(new ForwardNettyCommandHandler(nettyRemotingClient));
                    }
                });

        // 绑定端口，同步等待成功
        b.bind(NettyConstant.REMOTE_IP, NettyConstant.REMOTE_PORT).sync();
        System.out.println("Netty server start ok : "
                + (NettyConstant.REMOTE_IP + " : " + NettyConstant.REMOTE_PORT));
    }

    public static void main(String[] args) throws Exception {
        NettyRemotingClient nettyRemotingClient = new NettyRemotingClient();
        nettyRemotingClient.start();
        new ForwardNettyServer(nettyRemotingClient).bind();
    }
}
