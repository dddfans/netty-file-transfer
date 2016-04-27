package org.lqk.netty.server.forward;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.io.FileUtils;
import org.lqk.netty.protocol.NettyMessageType;
import org.lqk.netty.NettyConstant;
import org.lqk.netty.codec.marshalling.MarshallingEncoder;
import org.lqk.netty.forward.util.MD5Util;
import org.lqk.netty.protocol.NettyMessageHeader;
import org.lqk.netty.protocol.NettyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bert on 16-4-27.
 */
public class ForwardClient {
    private static Logger log = LoggerFactory.getLogger(ForwardClient.class);

    public static void main(String[] args) throws Exception {

        Socket socket = new Socket(NettyConstant.REMOTE_IP,NettyConstant.REMOTE_PORT);
        OutputStream out = socket.getOutputStream();
        File file = new File("/home/bert/abc.mp3");
        NettyMessage infoMessage = fileInfoMessage(file);
        sendMessage(infoMessage,out);

        NettyMessage dataMessage = fileDataMessage(file);
        sendMessage(dataMessage,out);
        Thread.sleep(10000);
        out.close();
        socket.close();
    }

    public static NettyMessage fileDataMessage(File file) throws IOException {
        byte[] b = FileUtils.readFileToByteArray(file);
        NettyMessage message = new NettyMessage();
        NettyMessageHeader header = new NettyMessageHeader();
        Map<String,Object> attachment = new HashMap<String, Object>();
        header.setAttachment(attachment);
        header.setType(NettyMessageType.FILE_DATA_REQ.value());
        attachment.put("fileName",file.getName());
        message.setHeader(header);
        message.setBody(b);
        return message;
    }

    public static NettyMessage fileInfoMessage(File file) throws IOException {

        NettyMessage message = new NettyMessage();
        NettyMessageHeader header = new NettyMessageHeader();
        message.setHeader(header);
        Map<String,Object> attachment = new HashMap<String, Object>();
        header.setAttachment(attachment);
        header.setType(NettyMessageType.FILE_INFO_REQ.value());
        attachment.put("fileName",file.getName());
        attachment.put("md5", MD5Util.md5(file));
        attachment.put("fileSize",file.length());
        return message;

    }

    public static void sendMessage(NettyMessage message,OutputStream out) throws Exception {
        ByteBuf byteBuf = Unpooled.buffer(4 * 1024 * 1024);

        NettyMessage.writeNettyMessage(message,byteBuf,new MarshallingEncoder());
        byte[] b = new byte[byteBuf.readableBytes()];
        log.debug("send {} bytes",byteBuf.readableBytes());
        byteBuf.readBytes(b);

        out.write(b);
        out.flush();
        byteBuf.clear();
    }
}
