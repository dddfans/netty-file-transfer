package org.lqk.netty.zk.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.lqk.netty.zk.Constant;
import org.lqk.netty.zk.ServerServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * 使用zookeeper作为"config"中心,使用apache-curator方法库来简化zookeeper开发
 */
public class ZookeeperServerServiceAddressProvider implements ServerServiceAddressProvider {

    // 确保客户端已经从zk中拿到数据后，该类才初始化完毕
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    // 注册服务
    private ServerServiceType serverServiceType;
    // 服务版本号
    private String version = "1.0.0";

    private PathChildrenCache cachedPath;

    private CuratorFramework zkClient;

    // 用来保存当前provider所接触过的地址记录
    // 当zookeeper集群故障时,可以使用trace中地址,作为"备份"
    private Set<String> trace = new HashSet<String>();

    private final List<InetSocketAddress> container = new ArrayList<InetSocketAddress>();

    private Queue<InetSocketAddress> inner = new LinkedList<InetSocketAddress>();
    /*
	 * trace存储当前可用的server列表，当curator故障时，作为客户端本地的缓存。curator正常时，每次都正常更新
	 * 最开始将所有server加入到inner中，每次客户端请求时，inner取出一个，取完了。再将container中的server列表全部加入到inner
	 * 使用queue的可能原因时，这样可以确保新的请求过来，轮询使用container中的server
	 */

    // 确保zk线程与业务线程访问inner与container时互斥
    private Object lock = new Object();

    private static Logger log = LoggerFactory.getLogger(ZookeeperServerServiceAddressProvider.class);

    public ZookeeperServerServiceAddressProvider() {
    }

    public ZookeeperServerServiceAddressProvider(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    public void setService(ServerServiceType serverServiceType) {
        this.serverServiceType = serverServiceType;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setZkClient(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    public void afterPropertiesSet() throws Exception {
        // 如果zk尚未启动,则启动
        if (zkClient.getState() == CuratorFrameworkState.LATENT) {
            zkClient.start();
        }
        buildPathChildrenCache(zkClient, getServicePath(), true);
        cachedPath.start(StartMode.POST_INITIALIZED_EVENT);
        countDownLatch.await();
    }

    private String getServicePath() {
        return "/" + serverServiceType.name().toLowerCase() + "/" + version;
    }

    private void buildPathChildrenCache(final CuratorFramework client, String path, Boolean cacheData) throws Exception {
        cachedPath = new PathChildrenCache(client, path, cacheData);
        cachedPath.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                PathChildrenCacheEvent.Type eventType = event.getType();
                switch (eventType) {
                    case CONNECTION_RECONNECTED:
                        log.info("Connection is reconection.");
                        break;
                    case CONNECTION_SUSPENDED:
                        log.info("Connection is suspended.");
                        break;
                    case CONNECTION_LOST:
                        log.warn("Connection error,waiting...");
                        return;
                    case INITIALIZED:
                        //	countDownLatch.countDown();
                        log.warn("Connection init ...");
                    default:
                        //
                }
                // 任何节点的时机数据变动,都会rebuild,此处为一个"简单的"做法.
                cachedPath.rebuild();
                rebuild();
                countDownLatch.countDown();
            }

            protected void rebuild() throws Exception {
                List<ChildData> children = cachedPath.getCurrentData();
                if (children == null || children.isEmpty()) {
                    // 有可能所有的 server都与zookeeper断开了链接
                    // 但是,有可能, client与 server之间的网络是良好的
                    // 因此此处是否需要清空container,是需要多方面考虑的.
                    container.clear();
                    log.error("no active server or server-cluster error....");
                    return;
                }
                List<InetSocketAddress> current = new ArrayList<InetSocketAddress>();
                String path = null;
                for (ChildData data : children) {
                    path = data.getPath();
                    log.debug("get path:" + path);
                    path = path.substring(getServicePath().length() + 1);
                    log.debug("get serviceAddress:" + path);
                    String address = new String(path.getBytes(), "utf-8");
                    current.addAll(transfer(address));
                    trace.add(address);
                }
                Collections.shuffle(current);
                synchronized (lock) {
                    container.clear();
                    container.addAll(current);
                    inner.clear();
                    inner.addAll(current);

                }
            }
        });
    }

    private List<InetSocketAddress> transfer(String address) {
        String[] hostname = address.split(":");
        Integer weight = Constant.DEFAULT_WEIGHT;
        if (hostname.length == 3) {
            weight = Integer.valueOf(hostname[2]);
        }
        String ip = hostname[0];
        Integer port = Integer.valueOf(hostname[1]);
        List<InetSocketAddress> result = new ArrayList<InetSocketAddress>();
        // 根据优先级，将ip：port添加多次到地址集中，然后随机取地址实现负载
        for (int i = 0; i < weight; i++) {
            result.add(new InetSocketAddress(ip, port));
        }
        return result;
    }

    public List<InetSocketAddress> findServerAddressList() {
        return Collections.unmodifiableList(container);
    }

    public synchronized InetSocketAddress selector() {
        if (inner.isEmpty()) {
            if (!container.isEmpty()) {
                inner.addAll(container);
            } else if (!trace.isEmpty()) {
                synchronized (lock) {
                    for (String hostname : trace) {
                        container.addAll(transfer(hostname));
                    }
                    Collections.shuffle(container);
                    inner.addAll(container);
                }
            }
        }
        return inner.poll();
    }

    public void close() {
        try {
            cachedPath.close();
            zkClient.close();
        } catch (Exception e) {
        }
    }

    public ServerServiceType getService() {
        return serverServiceType;
    }


}
