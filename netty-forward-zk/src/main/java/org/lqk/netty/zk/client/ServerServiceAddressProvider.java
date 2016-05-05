package org.lqk.netty.zk.client;

import org.lqk.netty.zk.ServerServiceType;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * 从zookeeper中具体服务和版本的ip列表，根据一定策略选择一个
 */
public interface ServerServiceAddressProvider extends Closeable{

	void setService(ServerServiceType serverServiceType);
	
	//获取服务名称
	ServerServiceType getService();

	void setVersion(String version);

	/**
	 * 获取所有服务端地址
	 * @return
	 */
    List<InetSocketAddress> findServerAddressList();

    /**
     * 选取一个合适的address,可以随机获取等'
     * 内部可以使用合适的算法.
     * @return
     */
    InetSocketAddress selector();

	void afterPropertiesSet() throws Exception ;
}
