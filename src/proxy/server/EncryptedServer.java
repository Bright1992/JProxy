package proxy.server;

import encryptor.Encryptor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import proxy.handler.EncryptedServerInboundHandler;

public class EncryptedServer extends AbstractServer {
    private String password;
    private Encryptor encryptor;
    public EncryptedServer(int port, String encryptMethod, String password, int timeout, int backlog) throws Exception{
        super(port,timeout,backlog);
        this.password=password;
        this.encryptor=new Encryptor(encryptMethod,password);
    }

    @Override
    protected ChannelInitializer getHandler(){
        return new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline()
                        .addLast(new LengthFieldBasedFrameDecoder(65536,0,2,0,2))
                        .addLast(new EncryptedServerInboundHandler(encryptor));
            }
        };
    }
}
