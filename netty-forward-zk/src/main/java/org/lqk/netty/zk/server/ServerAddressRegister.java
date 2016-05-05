package org.lqk.netty.zk.server;

import org.apache.curator.framework.CuratorFramework;
import org.lqk.netty.zk.ServerServiceType;

import java.io.Closeable;

/**
 * 发布服务地址及端口到服务注册中心
 */
public interface ServerAddressRegister extends Closeable{
	/**
	 * 发布服务接口
	 * @param service 服务接口名称，一个产品中不能重复
	 * @param version 服务接口的版本号，默认1.0.0
	 * @param address 服务发布的地址和端口
	 */
	void register(ServerServiceType service, String version, String address);

	void setZkClient(CuratorFramework zkClient);
}
