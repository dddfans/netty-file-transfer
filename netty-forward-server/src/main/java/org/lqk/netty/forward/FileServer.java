package org.lqk.netty.forward;

import org.lqk.netty.zk.*;
import org.lqk.netty.zk.Constant;
import org.lqk.netty.zk.server.LocalNetworkServerAddressResolver;
import org.lqk.netty.zk.server.ServerAddressRegister;
import org.lqk.netty.zk.server.ServerAddressResolver;
import org.lqk.netty.zk.server.ZookeeperServerAddressRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Administrator on 2015/9/1.
 */
public class FileServer implements Closeable{

    private final NettyCommandServer server;

    private int port;
    //服务版本号
    private String version = "1.0.0";

    private ServerAddressResolver serverAddressResolver;

    private ServerAddressRegister serverAddressRegister;

    private ZookeeperClientFactory zookeeperClientFactory;

    private static Logger log = LoggerFactory.getLogger(FileServer.class);

    public FileServer(int port, String targetDir) throws Exception {
        this.port = port;

        server = new NettyCommandServer(port, targetDir);

        serverAddressResolver = new LocalNetworkServerAddressResolver();

        zookeeperClientFactory = new ZookeeperClientFactory();
        zookeeperClientFactory.setZkHosts("192.168.3.7:2181");
        zookeeperClientFactory.setNamespace("file-transfer");
        zookeeperClientFactory.setConnectionTimeout(3000);
        zookeeperClientFactory.setSessionTimeout(3000);
        zookeeperClientFactory.setSingleton(true);

        serverAddressRegister = new ZookeeperServerAddressRegister();
        serverAddressRegister.setZkClient(zookeeperClientFactory.getObject());

    }

    public void start() throws Exception {
        server.start();
        String ip = serverAddressResolver.getServerIp();
        String address = ip + ":" + port + ":" + Constant.DEFAULT_WEIGHT;
        serverAddressRegister.register(ServerServiceType.AUDIO, version, address);
    }

    public void close() throws IOException {
        zookeeperClientFactory.close();
        serverAddressRegister.close();
    }
}
