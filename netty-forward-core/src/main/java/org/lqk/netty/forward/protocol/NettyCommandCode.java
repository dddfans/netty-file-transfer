package org.lqk.netty.forward.protocol;

/**
 * Created by bert on 16-4-25.
 */
public enum NettyCommandCode {
    FILE_INFO(1),FILE_SEGMENT(2),LAST_FILE_SEGMENT(3);


    private int code;
    // 构造方法
    private NettyCommandCode(int code) {
        this.code = code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static NettyCommandCode valueOf(int value) {
        switch (value) {
            case 1:
                return FILE_INFO;
            case 2:
                return FILE_SEGMENT;
            case 3:
                return LAST_FILE_SEGMENT;
            default:
                return null;
        }
    }
}
