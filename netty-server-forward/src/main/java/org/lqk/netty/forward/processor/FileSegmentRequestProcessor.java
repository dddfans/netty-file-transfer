package org.lqk.netty.forward.processor;

import org.lqk.netty.forward.file.FileState;
import org.lqk.netty.forward.file.FileStorage;
import org.lqk.netty.forward.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

public class FileSegmentRequestProcessor extends AbstractFileRequestProcessor{
	
	private CRC32 crc32 = new CRC32();
	
	private static Logger log = LoggerFactory.getLogger(FileInfoRequestProcessor.class);
	public FileSegmentRequestProcessor(ConcurrentHashMap<String, FileStorage> fileTable, String baseDir, int NettyCommandCode) {
		super(fileTable,baseDir,NettyCommandCode);
	}

	@Override
	public NettyCommand process(NettyCommand command) throws IOException {
		FileSegmentRequest body = (FileSegmentRequest) command.getBody();
		
		NettyCommand response = new NettyCommand(NettyCommandCode.FILE_SEGMENT.getCode(), NettyCommandType.File_Response.getCode(), false,command.getOpaque());
		response.setOpaque(command.getOpaque());
		FileSegmentResponse segmentResponse = new FileSegmentResponse();
		response.setBody(segmentResponse);
		segmentResponse.setFileName(body.getFileName());
		segmentResponse.setPosition(body.getPosition());
		segmentResponse.setBlockSize(body.getBlockSize());
		
		byte[] b = body.getContent();
		long expectCrc32 = body.getCrc32();
		
		crc32.update(b);
		long realCrc32 = crc32.getValue();
		crc32.reset();
		if (expectCrc32 == realCrc32) {
			String fileName = body.getFileName();
			FileStorage fileStorage = fileTable.get(fileName);
			fileStorage.write(body.getPosition(), b,0,body.getBlockSize());
			segmentResponse.setBlockState(true);
			if (FileState.FINISHED == fileStorage.state()) {
				log.debug("finished");
				fileStorage.close();
				fileTable.remove(fileName);
				segmentResponse.setFileState(true);
			}else{
				segmentResponse.setFileState(false);
			}

		} else {
			segmentResponse.setBlockState(false);
			segmentResponse.setFileState(false);
			log.debug("wrong crc32 at position {},expectCrc32 {},realCrc32 {}",body.getPosition(),expectCrc32,realCrc32);
		}
		
		return response;
	}

}
