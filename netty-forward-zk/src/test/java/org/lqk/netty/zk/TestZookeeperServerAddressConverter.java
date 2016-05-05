package org.lqk.netty.zk;

import org.lqk.netty.zk.client.ZookeeperServerAddressConverter;
import org.lqk.netty.zk.client.ZookeeperServerAddressProvider;

import java.net.InetSocketAddress;

/**
 * Created by bert on 16-5-5.
 */
public class TestZookeeperServerAddressConverter {
    public static void main(String[] args) throws Exception {
        ZookeeperServerAddressProvider zookeeperServerAddressProvider = new ZookeeperServerAddressProvider();
        ZookeeperServerAddressConverter zookeeperServerAddressConverter = new ZookeeperServerAddressConverter(zookeeperServerAddressProvider);
        InetSocketAddress address = zookeeperServerAddressConverter.convert("1:1");
        System.out.println(address.toString());
    }
}
