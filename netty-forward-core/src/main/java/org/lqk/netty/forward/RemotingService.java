package org.lqk.netty.forward;

import java.io.IOException;

/**
 * Created by Administrator on 2015/8/25.
 */
public interface RemotingService {

    /**
     * 启动远程服务
     * @throws Exception
     */
    void start();

    /**
     * 关闭远程服务
     * @throws Exception
     */
    void stop() throws IOException;

}
