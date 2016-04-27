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
package org.lqk.netty.protocol;

import io.netty.buffer.ByteBuf;
import org.lqk.netty.codec.marshalling.MarshallingDecoder;
import org.lqk.netty.codec.marshalling.MarshallingEncoder;

import java.util.Map;

/**
 * @author lilinfeng
 * @date 2014年3月14日
 * @version 1.0
 */
public final class NettyMessage {

	private NettyMessageHeader header;

	private byte[] body;

	public static NettyMessage readNettyMessage(NettyMessage nettyMessage, ByteBuf byteBuf, MarshallingDecoder marshallingDecoder) throws Exception {
		if(null == nettyMessage.getHeader()){
			nettyMessage.setHeader(new NettyMessageHeader());
		}
		NettyMessageHeader.readHeader(nettyMessage.getHeader(),byteBuf,marshallingDecoder);
		return readBody(nettyMessage,byteBuf);
	}

	public static NettyMessage readBody(NettyMessage nettyMessage, ByteBuf byteBuf) {
		int bodyLen = nettyMessage.getHeader().getBodyLength();
		if (byteBuf.readableBytes() > 0 && bodyLen > 0) {
			byte[] b = new byte[bodyLen];
			byteBuf.readBytes(b);
			nettyMessage.setBody(b);
		}
		return nettyMessage;
	}


	public static ByteBuf writeNettyMessage(NettyMessage nettyMessage,ByteBuf byteBuf,MarshallingEncoder marshallingEncoder) throws Exception {
		NettyMessageHeader.writeHeader(nettyMessage.getHeader(),byteBuf,marshallingEncoder);
		if (nettyMessage.getBody() != null) {
			byteBuf.writeBytes(nettyMessage.getBody());
			byteBuf.setInt(NettyMessageHeader.BODY_LENGTH_OFFSET, nettyMessage.getBody().length);
		}else{
			byteBuf.setInt(NettyMessageHeader.BODY_LENGTH_OFFSET, 0);
		}
		byteBuf.setInt(NettyMessageHeader.LENGTH_OFFSET, byteBuf.readableBytes());
		return byteBuf;
	}

	/**
	 * @return the header
	 */
	public final NettyMessageHeader getHeader() {
		return header;
	}

	/**
	 * @param header
	 *            the header to set
	 */
	public final void setHeader(NettyMessageHeader header) {
		this.header = header;
	}

	/**
	 * @return the body
	 */
	public final byte[] getBody() {
		return body;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	public final void setBody(byte[] body) {
		this.body = body;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "NettyMessage [header=" + header + "]";
	}
}
