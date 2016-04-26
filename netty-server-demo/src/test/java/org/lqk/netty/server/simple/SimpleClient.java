package org.lqk.netty.server.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.io.FileUtils;
import org.lqk.netty.NettyConstant;
import org.lqk.netty.codec.marshalling.MarshallingEncoder;
import org.lqk.netty.protocol.Header;
import org.lqk.netty.protocol.NettyMessage;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bert on 16-4-26.
 */
public class SimpleClient {

    public static void main(String[] args) throws Exception {
        NettyMessage nettyMessage = message("/home/bert/abc.mp3");

        Socket socket = new Socket(NettyConstant.REMOTE_IP,NettyConstant.REMOTE_PORT);

        OutputStream out = socket.getOutputStream();

        ByteBuf byteBuf = Unpooled.buffer(4 * 1024 * 1024);

        NettyMessage.writeNettyMessage(nettyMessage,byteBuf,new MarshallingEncoder());

        byte[] b = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(b);

        out.write(b);
        out.flush();

        Thread.sleep(10000);
        out.close();
    }

    public static NettyMessage message(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] b = FileUtils.readFileToByteArray(file);
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        Map<String,Object> attachment = new HashMap<String, Object>();
        header.setAttachment(attachment);
        attachment.put("fileName",file.getName());

        message.setHeader(header);
        message.setBody(b);
        return message;
    }
}
