package org.lqk.netty.forward.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * Created by bert on 16-5-4.
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private FileResponseProcessorDispatcher fileResponseProcessorDispatcher;

    public ClientChannelInitializer(FileResponseProcessorDispatcher fileResponseProcessorDispatcher){
        this.fileResponseProcessorDispatcher = fileResponseProcessorDispatcher;
    }
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(
                new ObjectEncoder(),
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                new NettyRemotingClientHandler(fileResponseProcessorDispatcher)
        );
    }
}
