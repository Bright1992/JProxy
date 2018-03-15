package proxy.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.HttpRequestEncoder;

public class ServerRemoteInitializer extends ChannelInitializer {
    boolean isHttps=false;
    ChannelHandlerContext ctx;
    public ServerRemoteInitializer(ChannelHandlerContext ctx){
        this(ctx,false);
    }
    public ServerRemoteInitializer(ChannelHandlerContext ctx, boolean isHttps){
        this.ctx=ctx;
        this.isHttps=isHttps;
    }
    @Override
    public void initChannel(Channel ch) throws Exception{
        if(!isHttps){
            ch.pipeline()
                    .addLast("encoder", new HttpRequestEncoder())
                    .addBefore("encoder","check", new ChannelOutboundHandlerAdapter() {
                        @Override
                        public void write(ChannelHandlerContext ctx0, Object msg, ChannelPromise promise) throws Exception {
                            ctx0.writeAndFlush(msg,promise);
                            ctx0.pipeline().remove("encoder");
                            ctx0.pipeline().remove("check");
                            ctx.pipeline().remove("encoder");
                            ctx.pipeline().remove("decoder");
                            ctx.pipeline().remove("aggregator");
                        }
                    });
        }
        ch.pipeline().addLast(
                new DirectRelayChannelHandler(ctx)
        );

    }
}
