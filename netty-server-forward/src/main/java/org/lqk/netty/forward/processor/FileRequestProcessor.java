package org.lqk.netty.forward.processor;

import org.lqk.netty.forward.protocol.NettyCommand;

import java.io.IOException;

public interface FileRequestProcessor {
	NettyCommand process(NettyCommand command)  throws IOException;
	int getCmdCode();
}
