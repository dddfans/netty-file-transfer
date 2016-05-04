package org.lqk.netty.forward.server.processor;


import org.lqk.netty.forward.server.file.FileStorage;
import org.lqk.netty.forward.protocol.NettyCommand;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractFileRequestProcessor implements FileRequestProcessor{
	
	protected ConcurrentHashMap<String, FileStorage> fileTable;

	protected final String baseDir;
	// 支持的类型
	private final int[] cmdCodes;
	
	public AbstractFileRequestProcessor(ConcurrentHashMap<String, FileStorage> fileTable,String baseDir,int[] cmdCodes){
		this.fileTable = fileTable;
		this.baseDir = baseDir;
		this.cmdCodes = cmdCodes;
	}
	
	public abstract NettyCommand process(NettyCommand command)  throws IOException;
	public int[] getCmdCodes() {
		return cmdCodes;
	}
	
	

}
