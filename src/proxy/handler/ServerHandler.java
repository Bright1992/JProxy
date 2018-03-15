package proxy.handler;

import exceptions.ProtocolNotSupportedException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import util.Status;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    ChannelFuture conn;
    String host;
    int port=80;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(conn!=null) {
            conn.channel().writeAndFlush(msg);
        }
        else if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String temp[] = request.headers().get("host").split(":");
            if (temp.length == 2) {
                host = temp[0];
                port = Integer.valueOf(temp[1]);
            } else if (temp.length == 1) {
                host = temp[0];
            } else {
                System.out.println("No host and port");
                return;
            }
            Bootstrap b = new Bootstrap();
            boolean isHttps=request.method().name().equalsIgnoreCase("CONNECT");
            b.group(ctx.channel().eventLoop())
                    .channel(ctx.channel().getClass())
                    .handler(new ServerRemoteInitializer(ctx,isHttps));

            conn = b.connect(temp[0], port);
            conn.addListener(
                    new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture cf) throws Exception {
                            if(cf.isSuccess()){
                                if(isHttps) {
                                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, Status.SUCCESS);
                                    ctx.writeAndFlush(response);
                                    ctx.pipeline().remove("encoder");
                                    ctx.pipeline().remove("decoder");
                                    ctx.pipeline().remove("aggregator");
                                }
                                if(!isHttps) {
                                    cf.channel().writeAndFlush(msg);
                                }
                            }
                            else {
                                ctx.channel().close();
                            }
                        }
                    }
            );
        } else {
                throw new ProtocolNotSupportedException("Not HTTP or HTTPS package");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        ctx.channel().close();
        if(cause.getMessage().equalsIgnoreCase("Connection reset by peer"))
            System.err.println("Connection reset by peer");
        else
            cause.printStackTrace();
    }
}
