package proxy.server;

import proxy.AbstractProxy;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Interface of all proxy servers.
 */
public abstract class AbstractServer implements AbstractProxy {
    int port;
    int backlog;
    int timeout;

    public AbstractServer(int port, int timeout, int backlog){
        this.port=port;
        this.timeout=timeout;
        this.backlog=backlog;
    }

    public AbstractServer setPort(int port) {
        this.port = port;
        return this;
    }

    public AbstractServer setBacklog(int backlog) {
        this.backlog = backlog;
        return this;
    }

    @Override
    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(getHandler())
                    .option(ChannelOption.SO_BACKLOG, 127);
            ChannelFuture cf = bootstrap.bind(port).sync();
            cf.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    protected abstract ChannelInitializer getHandler();
}
