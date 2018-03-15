package test;

import proxy.server.HTTPServer;

public class HTTPServerTest {
    public static void main(String[] args) throws Exception {
        new HTTPServer(1081, 22222,127).start();
    }
}
