package proxy.handler;

import encryptor.Encryptor;
import exceptions.UndefinedStateException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang.ArrayUtils;
import util.State;
import util.Utils;

public class EncryptedServerInboundHandler extends ServerHandler {
    private Encryptor encryptor;
    public EncryptedServerInboundHandler(Encryptor encryptor){
        super();
        this.encryptor = encryptor;
    }

    private int state=State.ADDRESSING;

    private ChannelFuture conn = null;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        ByteBuf bb = (ByteBuf)msg;
        byte[] raw = Utils.decryptBuf(bb,encryptor);
        bb.release();
        if(conn==null&&state== State.ADDRESSING){
            Object[] addr= Utils.parseAddress(raw);
            String ip=(String)addr[0];
            int port=(Integer)addr[1];
            Bootstrap b = new Bootstrap();
            b.group(ctx.channel().eventLoop())
                    .channel(ctx.channel().getClass())
                    .handler(new EncryptedServerRemoteInitializer(ctx,encryptor));
            conn = b.connect(ip,port);
            byte[] content = ArrayUtils.subarray(raw,(Integer)addr[2],raw.length);
            conn.addListener(
                    new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture cf) throws Exception {
                            ByteBuf rqst = ByteBufAllocator.DEFAULT.buffer();
                            rqst.writeBytes(content);
                            cf.channel().writeAndFlush(rqst);
                        }
                    }
            );
            state=State.STREAMING;
        }
        else if(conn!=null){
            ByteBuf rqst = ByteBufAllocator.DEFAULT.buffer();
            rqst.writeBytes(raw);
            conn.channel().writeAndFlush(rqst);
        }
        else
            throw new UndefinedStateException("Unsupported state: "+state);

        //TODO: Close the connection if timeout?
    }
}
