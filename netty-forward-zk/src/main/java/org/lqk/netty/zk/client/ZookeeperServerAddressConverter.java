package org.lqk.netty.zk.client;

import org.lqk.netty.zk.ServerServiceType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * key format : sequenceId : server_service_type
 * Created by bert on 16-5-5.
 */
public class ZookeeperServerAddressConverter implements ServerAddressConverter{
    private Map<String,InetSocketAddress> cache = new ConcurrentHashMap();

    private ServerAddressProvider serverAddressProvider;

    public ZookeeperServerAddressConverter(ServerAddressProvider serverAddressProvider){
        this.serverAddressProvider = serverAddressProvider;
    }

    public InetSocketAddress convert(String key){
        // 如果key在缓存中，则直接返回，否则寻找合适的address并返回
        InetSocketAddress address = cache.get(key);
        if(null != address){
            return address;
        }
        String[] strs = key.split(":");
        String sequenceId = strs[0];
        ServerServiceType serverServiceType = ServerServiceType.valueOf(Integer.parseInt(strs[1]));
        address = serverAddressProvider.selector(serverServiceType);
        cache.put(key,address);
        return address;
    }

    @Override
    public void close() throws IOException {
        serverAddressProvider.close();
    }
}
