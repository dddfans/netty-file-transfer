package org.lqk.netty.server.complex;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.io.FileUtils;
import org.lqk.netty.MessageType;
import org.lqk.netty.NettyConstant;
import org.lqk.netty.codec.marshalling.MarshallingEncoder;
import org.lqk.netty.protocol.Header;
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
 * Created by bert on 16-4-26.
 */
public class ComplexClient {

    private static Logger log = LoggerFactory.getLogger(ComplexClient.class);

    public static void main(String[] args) throws Exception {

        Socket socket = new Socket(NettyConstant.REMOTE_IP,NettyConstant.REMOTE_PORT);
        OutputStream out = socket.getOutputStream();
        NettyMessage infoMessage = fileInfoMessage("abc.mp3");
        sendMessage(infoMessage,out);

        NettyMessage dataMessage = fileDataMessage("/home/bert/abc.mp3");
        sendMessage(dataMessage,out);
        Thread.sleep(10000);
        out.close();
        socket.close();
    }

    public static NettyMessage fileDataMessage(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] b = FileUtils.readFileToByteArray(file);
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        Map<String,Object> attachment = new HashMap<String, Object>();
        header.setAttachment(attachment);
        header.setType(MessageType.FILE_DATA_REQ.value());
        attachment.put("fileName",file.getName());
        message.setHeader(header);
        message.setBody(b);
        return message;
    }

    public static NettyMessage fileInfoMessage(String fileName) throws IOException {

        NettyMessage message = new NettyMessage();
        Header header = new Header();
        message.setHeader(header);
        Map<String,Object> attachment = new HashMap<String, Object>();
        header.setAttachment(attachment);
        header.setType(MessageType.FILE_INFO_REQ.value());
        attachment.put("fileName",fileName);
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
