package proxy.client;

import encryptor.Encryptor;
import proxy.handler.EncryptedClientInboundHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class EncryptedClient extends AbstractClient {
    Encryptor encryptor;
    public EncryptedClient(int localPort, String remoteHost, int remotePort, String encryptMethod, String password, int timeout, int backlog) throws Exception{
        super(localPort,remoteHost,remotePort,timeout,backlog);
        encryptor=new Encryptor(encryptMethod,password);
    }

    @Override
    public void start() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup(),
                workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(getHandler())
                    .option(ChannelOption.SO_BACKLOG, backlog)
                    .option(ChannelOption.SO_TIMEOUT, timeout);
            ChannelFuture cf = b.bind(localPort).sync();
            cf.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public ChannelInitializer getHandler(){
        return new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline()
                        .addLast("encoder",new HttpResponseEncoder())
                        .addLast("decoder",new HttpRequestDecoder())
                        .addLast("aggregator",new HttpObjectAggregator(65536))
                        .addLast(new EncryptedClientInboundHandler(remoteHost,remotePort,encryptor));
            }
        };
    }
}
