package org.lqk.netty.zk;

/**
 * Created by bert on 16-5-5.
 */
public enum ServerServiceType {
    AUDIO(1), IMAGE(2);

    private int code;

    // 构造方法
    private ServerServiceType(int code) {
        this.code = code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ServerServiceType valueOf(int value) {
        switch (value) {
            case 1:
                return AUDIO;
            case 2:
                return IMAGE;
            default:
                return null;
        }
    }
}
