package org.lqk.netty.forward.protocol;

import java.io.Serializable;

public class FileSegmentResponse implements NettyCommandBody, Serializable {

    private String fileName;

    private long position;

    private int blockSize;

    private boolean blockState;
    private boolean fileState;

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

    public boolean isBlockState() {
        return blockState;
    }

    public void setBlockState(boolean blockState) {
        this.blockState = blockState;
    }

    public boolean isFileState() {
        return fileState;
    }

    public void setFileState(boolean fileState) {
        this.fileState = fileState;
    }

}
