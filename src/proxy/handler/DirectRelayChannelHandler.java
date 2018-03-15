package proxy.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class DirectRelayChannelHandler extends ChannelInboundHandlerAdapter {
    ChannelHandlerContext ctx;
    public DirectRelayChannelHandler(ChannelHandlerContext ctx){
        super();
        this.ctx=ctx;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx0, Object msg){
        ctx.channel().writeAndFlush(msg);
    }
}
