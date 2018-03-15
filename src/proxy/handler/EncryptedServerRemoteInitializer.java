package proxy.handler;

import encryptor.Encryptor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;

public class EncryptedServerRemoteInitializer extends ChannelInitializer {
    ChannelHandlerContext ctx;
    Encryptor encryptor;
    public EncryptedServerRemoteInitializer(ChannelHandlerContext ctx, Encryptor encryptor){
        super();
        this.ctx=ctx;
        this.encryptor = encryptor;
    }
    @Override
    public void initChannel(Channel ch) throws Exception{
        ch.pipeline().addLast(new EncryptedRelayChannelHandler(ctx,encryptor));
    }
}
