package org.lqk.netty.zk;

import org.lqk.netty.zk.client.ZookeeperServerAddressProvider;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by bert on 16-5-5.
 */
public class TestZookeeperServerAddressProvider {
    public static void main(String[] args) throws Exception {
        ZookeeperServerAddressProvider zookeeperServerAddressProvider = new ZookeeperServerAddressProvider();
        List<InetSocketAddress> addresses = zookeeperServerAddressProvider.findServerAddressList();

    }
}
