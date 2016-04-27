package org.lqk.netty.forward;

public class Constant {
	public final static int BLOCK_SIZE = 1024 * 1024;
	/*
	 * BLOCK作为object的content部分， MAX_OBJECT_SIZE必须比BLOCK_SIZE大一些
	 */

	public final static int MAX_OBJECT_SIZE = 4 * 1024 * 1024;

}
