package org.lqk.netty.forward.server.processor;

import org.apache.commons.io.FilenameUtils;
import org.lqk.netty.forward.server.file.FileState;
import org.lqk.netty.forward.server.file.FileStorage;
import org.lqk.netty.forward.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2015/8/30.
 */
public class FileInfoRequestProcessor extends AbstractFileRequestProcessor {

	
	private static Logger log = LoggerFactory.getLogger(FileInfoRequestProcessor.class);

	public FileInfoRequestProcessor(ConcurrentHashMap<String, FileStorage> fileTable, String baseDir, int[] nettyCommandCodes) {
		super(fileTable,baseDir,nettyCommandCodes);
	}

	
	public NettyCommand process(NettyCommand request) throws IOException {
		NettyCommand response = new NettyCommand(NettyCommandCode.FILE_INFO.getCode(), NettyCommandType.File_Response.getCode(), false,request.getOpaque());
		if (fileTable.size() >= 16) {
			response.setFlag(false);
			return response;
		}
		FileInfoRequest body = (FileInfoRequest) request.getBody();
		String fileName = body.getFileName();
		String filePath = FilenameUtils.normalize(baseDir + File.separator + fileName);
		RandomAccessFile file = new RandomAccessFile(filePath, "rw");

		FileStorage fileStorage = fileTable.get(fileName);

		if(null == fileStorage){
			fileStorage = new FileStorage(file.getChannel(), body.getFileSize(), body.getMd5(), body.getBlockSize());
			fileTable.put(fileName, fileStorage);
		}

		FileInfoResponse fileInfoResponse = new FileInfoResponse();
		fileInfoResponse.setFileName(fileName);
		fileInfoResponse.setBlockSize(fileStorage.getBlockSize());
		FileState fileState = fileStorage.isTimeout() ? FileState.INVALD : fileStorage.state() ;
		fileInfoResponse.setFileState(fileState.getValue());
		fileInfoResponse.setPosition(fileStorage.getPosition());
		response.setFlag(true);
		response.setBody(fileInfoResponse);
		return response;
	}
}
