package org.lqk.netty.zk.client;

import org.lqk.netty.zk.ServerServiceType;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * 以便构建客户端连接池
 */
public interface ServerAddressProvider extends Closeable{

    /**
     * 选取一个合适的address
     */
    InetSocketAddress selector(ServerServiceType serverServiceType);


	/**
	 * 获取所有服务端地址
	 * @return
	 */
	List<InetSocketAddress> findServerAddressList();
}
