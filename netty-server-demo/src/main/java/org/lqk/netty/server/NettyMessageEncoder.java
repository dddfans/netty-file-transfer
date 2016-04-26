/*
 * Copyright 2013-2018 Lilinfeng.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lqk.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.lqk.netty.codec.marshalling.MarshallingEncoder;
import org.lqk.netty.protocol.NettyMessage;

import java.io.IOException;

/**
 * @author Lilinfeng
 * @date 2014年3月14日
 * @version 1.0
 */
public final class NettyMessageEncoder extends MessageToByteEncoder<NettyMessage> {

	MarshallingEncoder marshallingEncoder;

	public NettyMessageEncoder() throws IOException {
		this.marshallingEncoder = new MarshallingEncoder();
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, NettyMessage msg, ByteBuf sendBuf) throws Exception {
		if (msg == null || msg.getHeader() == null)
			throw new Exception("The encode message is null");
		NettyMessage.writeNettyMessage(msg,sendBuf,marshallingEncoder);
	}
}
