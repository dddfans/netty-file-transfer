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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月14日
 */
public final class NettyMessageHeader {

    public final static int LENGTH_OFFSET = 4;
    public final static int BODY_LENGTH_OFFSET = 8;
    public final static int TYPE_OFFSET = 12;

    private int crcCode = 0xabef0101;

    private int length;// 消息长度

    private int bodyLength;// 消息体长度

    private byte type;// 消息类型

    private long sessionID;// 会话ID

    private byte priority;// 消息优先级

    private Map<String, Object> attachment = new HashMap<String, Object>(); // 附件


    public static ByteBuf writeHeader(NettyMessageHeader header, ByteBuf byteBuf, MarshallingEncoder marshallingEncoder) throws Exception {
        byteBuf.writeInt((header.getCrcCode()));
        byteBuf.writeInt((header.getLength()));
        byteBuf.writeInt((header.getBodyLength()));
        byteBuf.writeByte((header.getType()));
        byteBuf.writeLong((header.getSessionID()));
        byteBuf.writeByte((header.getPriority()));
        writeAttachment(header, byteBuf, marshallingEncoder);
        return byteBuf;
    }

    private static ByteBuf writeAttachment(NettyMessageHeader header, ByteBuf byteBuf, MarshallingEncoder marshallingEncoder) throws Exception {
        byteBuf.writeInt((header.getAttachment().size()));
        String key = null;
        byte[] keyArray = null;
        Object value = null;
        for (Map.Entry<String, Object> param : header.getAttachment().entrySet()) {
            key = param.getKey();
            keyArray = key.getBytes("UTF-8");
            byteBuf.writeInt(keyArray.length);
            byteBuf.writeBytes(keyArray);
            value = param.getValue();
            marshallingEncoder.encode(value, byteBuf);
        }
        key = null;
        keyArray = null;
        value = null;
        return byteBuf;
    }

    public static NettyMessageHeader readHeader(NettyMessageHeader header, ByteBuf byteBuf, MarshallingDecoder marshallingDecoder) throws Exception {
        header.setCrcCode(byteBuf.readInt());
        header.setLength(byteBuf.readInt());
        header.setBodyLength(byteBuf.readInt());
        header.setType(byteBuf.readByte());
        header.setSessionID(byteBuf.readLong());
        header.setPriority(byteBuf.readByte());
        return readAttachment(header, byteBuf, marshallingDecoder);
    }

    private static NettyMessageHeader readAttachment(NettyMessageHeader header, ByteBuf byteBuf, MarshallingDecoder marshallingDecoder) throws Exception {
        int size = byteBuf.readInt();
        if (size > 0) {
            Map<String, Object> attch = new HashMap<String, Object>(size);
            int keySize = 0;
            byte[] keyArray = null;
            String key = null;
            for (int i = 0; i < size; i++) {
                keySize = byteBuf.readInt();
                keyArray = new byte[keySize];
                byteBuf.readBytes(keyArray);
                key = new String(keyArray, "UTF-8");
                attch.put(key, marshallingDecoder.decode(byteBuf));
            }
            keyArray = null;
            key = null;
            header.setAttachment(attch);
        }
        return header;
    }

    /**
     * @return the crcCode
     */
    public final int getCrcCode() {
        return crcCode;
    }

    /**
     * @param crcCode the crcCode to set
     */
    public final void setCrcCode(int crcCode) {
        this.crcCode = crcCode;
    }

    /**
     * @return the length
     */
    public final int getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public final void setLength(int length) {
        this.length = length;
    }

    /**
     * @return the sessionID
     */
    public final long getSessionID() {
        return sessionID;
    }

    /**
     * @param sessionID the sessionID to set
     */
    public final void setSessionID(long sessionID) {
        this.sessionID = sessionID;
    }

    /**
     * @return the type
     */
    public final byte getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public final void setType(byte type) {
        this.type = type;
    }

    /**
     * @return the priority
     */
    public final byte getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public final void setPriority(byte priority) {
        this.priority = priority;
    }

    /**
     * @return the attachment
     */
    public final Map<String, Object> getAttachment() {
        return attachment;
    }

    /**
     * @param attachment the attachment to set
     */
    public final void setAttachment(Map<String, Object> attachment) {
        this.attachment = attachment;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "NettyMessageHeader [crcCode=" + crcCode + ", length=" + length + ", bodyLength=" + bodyLength + ", sessionID="
                + sessionID + ", type=" + type + ", priority=" + priority + ", attachment=" + attachment + "]";
    }


}
