package org.lqk.netty.forward;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.lqk.netty.forward.server.framework.FileRequestProcessorDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by Administrator on 2015/8/25.
 */
public class NettyCommandServer implements RemotingServer {

	private int port;

	private ServerBootstrap serverBootstrap;

	private final EventLoopGroup eventLoopGroupWorker;

	private final EventLoopGroup eventLoopGroupBoss;

	private FileRequestProcessorDispatcher processorDispatcher;

	private static Logger log = LoggerFactory.getLogger(NettyCommandServer.class);

	public NettyCommandServer(int port, String baseDir) {
		this.port = port;
		this.processorDispatcher = new FileRequestProcessorDispatcher(baseDir);
		this.serverBootstrap = new ServerBootstrap();
		this.eventLoopGroupWorker = new NioEventLoopGroup();
		this.eventLoopGroupBoss = new NioEventLoopGroup();
	}

	public void start() {
		ServerBootstrap server = this.serverBootstrap.group(eventLoopGroupBoss, eventLoopGroupWorker)
				.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel socketChannel) throws Exception {
						socketChannel.pipeline().addLast(new ObjectEncoder(),
								new ObjectDecoder(Constant.MAX_OBJECT_SIZE, ClassResolvers.cacheDisabled(null)),
								new NettyCommandServerHandler(processorDispatcher));
					}

				});

		try {
			ChannelFuture sync = server.bind(new InetSocketAddress(this.port)).sync();
		} catch (InterruptedException e) {
			throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e);
		}

	}

	public void stop() {

		this.eventLoopGroupBoss.shutdownGracefully();

		this.eventLoopGroupWorker.shutdownGracefully();
	}
}
