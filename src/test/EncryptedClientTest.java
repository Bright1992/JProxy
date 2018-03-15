package test;

import proxy.client.EncryptedClient;

/**
 * Created by bright on 18-1-9.
 */
public class EncryptedClientTest {
    public static void main(String[] args) throws Exception {
        new EncryptedClient(1081,"localhost",8080,"aes-256-cfb","wangbowenmax",1000,127).start();
    }
}
