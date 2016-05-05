package org.lqk.netty.forward;

/**
 * Created by bert on 16-4-27.
 */
public class TestFileServer {
    public static void main(String[] args) throws Exception {
        FileServer server = new FileServer(8000, "/home/bert/tmp1");
        server.start();
    }
}
