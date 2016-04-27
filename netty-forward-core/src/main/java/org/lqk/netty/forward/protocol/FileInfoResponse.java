package org.lqk.netty.forward.protocol;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/8/30.
 */
public class FileInfoResponse implements NettyCommandBody, Serializable {

    private String fileName;

    private long position;

    private int blockSize;

    private int fileState;


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public int getFileState() {
        return fileState;
    }

    public void setFileState(int fileState) {
        this.fileState = fileState;
    }
}
