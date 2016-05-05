package org.lqk.netty.zk.client;

import java.io.Closeable;
import java.net.InetSocketAddress;

/**
 * 负责实现用户传入的key到对应服务器ip的转换
 * Created by bert on 16-5-5.
 */
public interface ServerAddressConverter extends Closeable{
    InetSocketAddress convert(String key);
}
