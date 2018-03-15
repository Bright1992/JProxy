package proxy.handler;

import encryptor.Encryptor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import util.Utils;

public class EncryptedRelayChannelHandler extends ChannelInboundHandlerAdapter {
    ChannelHandlerContext ctx;
    Encryptor encryptor;
    public EncryptedRelayChannelHandler(ChannelHandlerContext ctx, Encryptor encryptor){
        super();
        this.ctx=ctx;
        this.encryptor = encryptor;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx0, Object msg) throws Exception{
        byte[] encrypted=Utils.encryptBuf((ByteBuf)msg,encryptor);
        ((ByteBuf)msg).release();
        ByteBuf resp = Utils.createFrame(encrypted);
        ctx.writeAndFlush(resp);
    }
}
