package org.lqk.netty.forward.protocol;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/8/30.
 */
public class FileInfoRequest implements NettyCommandBody, Serializable {
	private String filePath; // for client
	private String fileName;

	private long fileSize;

	private String md5;

	private int blockSize;

	public FileInfoRequest() {
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
}
