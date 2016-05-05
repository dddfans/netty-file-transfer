package org.lqk.netty.zk.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * key format : ip:port
 * Created by bert on 16-5-5.
 */
public class SimpleServerAddressConverter implements ServerAddressConverter{

    public InetSocketAddress convert(String key){
        String[] strs = key.split(":");
        return new InetSocketAddress(strs[0],Integer.parseInt(strs[1]));
    }

    @Override
    public void close() throws IOException {

    }
}
