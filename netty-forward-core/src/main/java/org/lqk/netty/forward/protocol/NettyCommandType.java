package org.lqk.netty.forward.protocol;

/**
 * Created by bert on 16-4-25.
 */
public enum NettyCommandType {
    File_Request(1), File_Response(2);

    private int code;

    // 构造方法
    private NettyCommandType(int code) {
        this.code = code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static NettyCommandType valueOf(int value) {
        switch (value) {
            case 1:
                return File_Request;
            case 2:
                return File_Response;
            default:
                return null;
        }
    }
}
