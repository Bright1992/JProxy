package test;
import proxy.server.EncryptedServer;

public class EncryptedServerTest {
    public static void main(String[] argv) throws Exception {
        new EncryptedServer(8080,"aes-256-cfb","wangbowenmax",1000,127).start();

    }
}
