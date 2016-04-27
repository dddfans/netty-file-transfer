package org.lqk.netty.forward.file;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.lqk.netty.forward.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.MessageDigest;
import java.util.BitSet;

/**
 * Created by Administrator on 2015/8/30.
 */
public class FileStorage {

    private FileChannel fileChannel;

    private volatile FileState fileState = FileState.INITIAL;

    private int blockSize;

    // 写文件缓冲区
    private ByteBuffer buffer = ByteBuffer.allocate(Constant.BLOCK_SIZE);

    private long fileSize;

    private volatile long position;

    private String md5;

    private MessageDigest md5Digest = null;

    private int totalCount;

    private volatile long lastModifyTimestamp;

    private BitSet segments;

    private static final int TIMEOUT_MILLS = 60 * 1000;

    private static Logger log = LoggerFactory.getLogger(FileStorage.class);

    public FileStorage(FileChannel fileChannel, long fileSize, String md5, int blockSize) {
        this.fileChannel = fileChannel;
        this.fileSize = fileSize;
        this.md5 = md5;
        this.md5Digest = DigestUtils.getMd5Digest();
        this.blockSize = blockSize;
        this.lastModifyTimestamp = System.currentTimeMillis();
        this.totalCount = (int) (fileSize / blockSize) + (fileSize % blockSize == 0 ? 0 : 1);
        this.segments = new BitSet(totalCount);
    }

    public FileState state() throws IOException {
        switch (fileState) {
            case INITIAL:
                this.fileState = FileState.PART;
                return FileState.PART;
            case PART:
                synchronized (segments) {
                    boolean isSegmentsFinished = segments.cardinality() >= totalCount;
                    if (!isSegmentsFinished) {
                        return FileState.PART;
                    } else {
                        this.fileState = FileState.WRITE_OK;
                    }
                }
                // 如果所有块已经写入完毕，顺势判断下是否FINISHED
            case WRITE_OK:
                synchronized (md5Digest) {
                    byte[] digest = md5Digest.digest();
                    String realMd5 = Hex.encodeHexString(digest);
                    boolean isMd5Finished = StringUtils.equals(this.md5, realMd5);
                    this.fileState = isMd5Finished ? FileState.FINISHED : FileState.INVALD;
                    return this.fileState;
                }
            case INVALD:
                return FileState.INVALD;
            case FINISHED:
                return FileState.FINISHED;
            default:
                log.error("error file state");
        }
        return null;
    }

    public long getPosition() {
        return position;
    }

    public void close() {
        IOUtils.closeQuietly(this.fileChannel);
    }

    public void write(long position, byte[] content, int off, int len) throws IOException {
        FileLock fileLock = fileChannel.lock();
        try {
            buffer.clear();
            buffer.put(content, off, len);
            buffer.flip();
            this.md5Digest.update(content, off, len);
            this.position = position;
            fileChannel.position(position);
            while (buffer.hasRemaining()) {
                fileChannel.write(buffer);
            }
            segments.set((int) (position / blockSize));
            this.lastModifyTimestamp = System.currentTimeMillis();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            fileLock.release();
        }
    }
    //TODO 需要一个定时任务，清理过期的状态
    public boolean isTimeout() {
        return System.currentTimeMillis() - lastModifyTimestamp > TIMEOUT_MILLS;
    }

    public int getBlockSize() {
        return blockSize;
    }
}
