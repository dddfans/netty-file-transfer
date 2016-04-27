package org.lqk.netty.forward.processor;


import org.lqk.netty.forward.file.FileStorage;
import org.lqk.netty.forward.protocol.NettyCommand;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractFileRequestProcessor implements FileRequestProcessor{
	
	protected ConcurrentHashMap<String, FileStorage> fileTable;

	protected final String baseDir;
	
	private final int cmdCode;
	
	public AbstractFileRequestProcessor(ConcurrentHashMap<String, FileStorage> fileTable,String baseDir,int cmdCode){
		this.fileTable = fileTable;
		this.baseDir = baseDir;
		this.cmdCode = cmdCode;
	}
	
	@Override
	public abstract NettyCommand process(NettyCommand command)  throws IOException;
	@Override
	public int getCmdCode() {
		return cmdCode;
	}
	
	

}
