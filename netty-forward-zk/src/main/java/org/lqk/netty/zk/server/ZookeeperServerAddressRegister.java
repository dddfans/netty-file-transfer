package org.lqk.netty.zk.server;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.CreateMode;
import org.lqk.netty.zk.ServerServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * 注册服务列表到Zookeeper
 */
public class ZookeeperServerAddressRegister implements ServerAddressRegister {

    private CuratorFramework zkClient;

    private static Logger log = LoggerFactory.getLogger(ZookeeperServerAddressRegister.class);

    public ZookeeperServerAddressRegister() {
    }

    public ZookeeperServerAddressRegister(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    public void setZkClient(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public void register(ServerServiceType service, String version, String address) {
        if (zkClient.getState() == CuratorFrameworkState.LATENT) {
            zkClient.start();
        }
        if (StringUtils.isEmpty(version)) {
            version = "1.0.0";
        }
        //临时节点
        try {
            String path = "/" + service.name().toLowerCase() + "/" + version + "/" + address;
            zkClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(path);
            log.debug("register {} into zookeeper",path);
        } catch (UnsupportedEncodingException e) {
            log.error("register service address to zookeeper exception:{}", e);
        } catch (Exception e) {
            log.error("register service address to zookeeper exception:{}", e);
        }
    }

    public void close() {
        zkClient.close();
    }
}
