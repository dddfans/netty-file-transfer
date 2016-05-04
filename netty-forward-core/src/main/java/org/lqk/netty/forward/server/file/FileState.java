package org.lqk.netty.forward.server.file;

/**
 * Created by bert on 16-4-27.
 */
public enum FileState {

    INITIAL(0), PART(1),WRITE_OK(2),INVALD(3),FINISHED(4);

    private int value;

    // 构造方法
    private FileState(int value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static FileState valueOf(int value) {
        switch (value) {
            case 0:
                return INITIAL;
            case 1:
                return PART;
            case 2:
                return WRITE_OK;
            case 3:
                return INVALD;
            case 4:
                return FINISHED;
            default:
                return null;
        }
    }
}
