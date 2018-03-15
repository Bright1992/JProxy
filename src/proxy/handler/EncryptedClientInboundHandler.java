package proxy.handler;

import encryptor.Encryptor;
import exceptions.ProtocolNotSupportedException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import util.Status;

public class EncryptedClientInboundHandler extends ChannelInboundHandlerAdapter {
    String remoteHost;
    int remotePort;
    Encryptor encryptor;
    public EncryptedClientInboundHandler(String remoteHost, int remotePort, Encryptor encryptor){
        super();
        this.remoteHost=remoteHost;
        this.remotePort=remotePort;
        this.encryptor=encryptor;
    }

    ChannelFuture conn=null;
    String destHost;
    int destPort=80;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        if(conn!=null){
            conn.channel().writeAndFlush(msg);
        }
        else if(msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            boolean isHttps = request.method().name().equalsIgnoreCase("CONNECT");
            String[] temp=request.headers().get("host").split(":");
            if(temp.length==2){
                destHost=temp[0];
                destPort=Integer.valueOf(temp[1]);
            }
            else if(temp.length==1){
                destHost=temp[0];
            }
            else
                throw new ProtocolNotSupportedException("undefined address format");

            Bootstrap b = new Bootstrap();
            b.group(ctx.channel().eventLoop())
                    .channel(ctx.channel().getClass())
                    .handler(new EncryptedClientRemoteInitializer(ctx,encryptor,isHttps,destHost,destPort));
            conn = b.connect(remoteHost,remotePort);
            conn.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture cf) throws Exception {
                    if(cf.isSuccess()){
                        if(isHttps){
                            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, Status.SUCCESS));
                            ctx.pipeline().remove("encoder");
                            ctx.pipeline().remove("decoder");
                            ctx.pipeline().remove("aggregator");
                            ((FullHttpRequest) msg).release();
                        }
                        else{
                            cf.channel().writeAndFlush(msg);
                        }
                    }
                    else
                        ctx.channel().closeFuture();
                }
            });
        }
        else{
            throw new ProtocolNotSupportedException("Not Http or Https");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e){
        if(e.getMessage().equalsIgnoreCase("connection reset by peer"))
            System.out.println("Connection reset by peer");
        else
            e.printStackTrace();
    }

}
