package org.lqk.netty.zk.client;

import org.lqk.netty.zk.ServerServiceType;
import org.lqk.netty.zk.ZookeeperClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 以便构建客户端连接池
 */
public class ZookeeperServerAddressProvider implements ServerAddressProvider{

    private Map<ServerServiceType,ServerServiceAddressProvider> typeMap = new ConcurrentHashMap<ServerServiceType,ServerServiceAddressProvider>();

    private ZookeeperClientFactory zookeeperClientFactory;

    private static Logger log = LoggerFactory.getLogger(ZookeeperServerAddressProvider.class);

    public ZookeeperServerAddressProvider() throws Exception {

        zookeeperClientFactory = new ZookeeperClientFactory();
        zookeeperClientFactory.setZkHosts("192.168.3.7:2181");
        zookeeperClientFactory.setNamespace("file-transfer");
        zookeeperClientFactory.setConnectionTimeout(3000);
        zookeeperClientFactory.setSessionTimeout(3000);
        zookeeperClientFactory.setSingleton(true);

        ServerServiceAddressProvider audioServerServiceAddressProvider = new ZookeeperServerServiceAddressProvider(zookeeperClientFactory.getObject());
        audioServerServiceAddressProvider.setService(ServerServiceType.AUDIO);
        audioServerServiceAddressProvider.setVersion("1.0.0");
        audioServerServiceAddressProvider.afterPropertiesSet();
        typeMap.put(ServerServiceType.AUDIO,audioServerServiceAddressProvider);

//        ServerServiceAddressProvider imageServerServiceAddressProvider = new ZookeeperServerServiceAddressProvider(zookeeperClientFactory.getObject());
//        imageServerServiceAddressProvider.setService(ServerServiceType.IMAGE);
//        imageServerServiceAddressProvider.setVersion("1.0.0");
//        imageServerServiceAddressProvider.afterPropertiesSet();
//        typeMap.put(ServerServiceType.IMAGE,imageServerServiceAddressProvider);
    }

    /**
     * 选取一个合适的address
     */
    public InetSocketAddress selector(ServerServiceType serverServiceType){
        ServerServiceAddressProvider serverServiceAddressProvider = typeMap.get(serverServiceType);
        log.debug("null == serverServiceAddressProvider ? {}", null == serverServiceAddressProvider);
        InetSocketAddress inetSocketAddress = serverServiceAddressProvider.selector();
        return inetSocketAddress;
    }

    @Override
    public List<InetSocketAddress> findServerAddressList() {
        List<InetSocketAddress> inetSocketAddresses = new ArrayList<InetSocketAddress>();
        for(ServerServiceType serverServiceType : typeMap.keySet()){
            ServerServiceAddressProvider serverServiceAddressProvider = typeMap.get(serverServiceType);
            List<InetSocketAddress> addresses = serverServiceAddressProvider.findServerAddressList();
            for(InetSocketAddress inetSocketAddress : addresses){
                log.debug("service {} address {}",serverServiceType.name(),inetSocketAddress.toString());
            }
            inetSocketAddresses.addAll(addresses);
        }
        return inetSocketAddresses;
    }

    @Override
    public void close() throws IOException {
        for(ServerServiceType serverServiceType : typeMap.keySet()){
            ServerServiceAddressProvider serverServiceAddressProvider = typeMap.get(serverServiceType);
            serverServiceAddressProvider.close();
        }
    }
}
