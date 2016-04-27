package org.lqk.netty.forward.protocol;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2015/8/25.
 */
public class NettyCommand implements Serializable{

    //请求序列，用于生成请求序列
	private static AtomicInteger REQUEST_ID = new AtomicInteger(0);
	
	public static int autoIncrement(){
		return REQUEST_ID.getAndIncrement();
	}
    //请求序列号
//    private int opaque = RequestId.getAndIncrement();
    private int opaque;
    //是否成功
    private boolean flag;
    // file task or segment
    private final int cmdCode;
    //请求类型   request response
    private final int type;

    //是否单向请求
    private final boolean rpcOneway;

    public static final int REQUEST_COMMAND = 1;
    public static final int RESPONSE_COMMAND = 2;

    //请求内容
    private String content;

    private NettyCommandBody body;
    /*
     * 强迫用户在省城command时指定opaque
     */
    public NettyCommand(int cmdCode, int type, boolean rpcOneway, int opaque) {
        this.cmdCode = cmdCode;
        this. type = type;
        this.rpcOneway = rpcOneway;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return  type;
    }

    public int getCmdCode() {
        return cmdCode;
    }

    public boolean isRpcOneway() {
        return rpcOneway;
    }

    public int getOpaque() {
        return opaque;
    }

    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }

    public NettyCommandBody getBody() {
        return body;
    }

    public void setBody(NettyCommandBody body) {
        this.body = body;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
