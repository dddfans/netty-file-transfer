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

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月15日
 */
public enum NettyMessageType {

    SERVICE_REQ((byte) 0), SERVICE_RESP((byte) 1), ONE_WAY((byte) 2), LOGIN_REQ(
            (byte) 3), LOGIN_RESP((byte) 4), HEARTBEAT_REQ((byte) 5), HEARTBEAT_RESP(
            (byte) 6), FILE_INFO_REQ((byte) 7), FILE_INFO_RESP((byte) 8), FILE_DATA_REQ((byte) 9),
    FILE_DATA_RESP((byte) 10), FILE_REQ_READ_TIMEOUT((byte) 11),FILE_SEGMENT_REQ((byte) 12);


    private byte value;

    private NettyMessageType(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }


    public static NettyMessageType valueOf(byte value) {
        switch (value) {
            case 0:
                return SERVICE_REQ;
            case 1:
                return SERVICE_RESP;
            case 2:
                return ONE_WAY;
            case 3:
                return LOGIN_REQ;
            case 4:
                return LOGIN_RESP;
            case 5:
                return HEARTBEAT_REQ;
            case 6:
                return HEARTBEAT_RESP;
            case 7:
                return FILE_INFO_REQ;
            case 8:
                return FILE_INFO_RESP;
            case 9:
                return FILE_DATA_REQ;
            case 10:
                return FILE_DATA_RESP;
            case 11:
                return FILE_REQ_READ_TIMEOUT;
            case 12:
                return FILE_SEGMENT_REQ;
            default:
                return null;
        }
    }
}
