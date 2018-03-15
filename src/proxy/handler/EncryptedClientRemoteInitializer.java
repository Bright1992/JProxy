package proxy.handler;

import encryptor.Encryptor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import util.State;
import util.Utils;

public class EncryptedClientRemoteInitializer extends ChannelInitializer {
    ChannelHandlerContext ctx;
    Encryptor encryptor;
    boolean isHttps = false;
    String destHost;
    int destPort;

    public EncryptedClientRemoteInitializer(ChannelHandlerContext ctx, Encryptor encryptor, boolean isHttps, String destHost, int destPort) {
        this.ctx = ctx;
        this.encryptor = encryptor;
        this.isHttps = isHttps;
        this.destHost = destHost;
        this.destPort = destPort;
    }

    @Override
    public void initChannel(Channel ch) throws Exception {
        ch.pipeline()
                //Outbound
                .addLast(new ChannelOutboundHandlerAdapter() {   //write to server
                    int state = State.ADDRESSING;

                    @Override
                    public void write(ChannelHandlerContext ctx0, Object msg, ChannelPromise promise) {
                        ByteBuf bb = (ByteBuf) msg;
                        if (state == State.ADDRESSING) {
                            byte[]raw = new byte[bb.readableBytes()];
                            bb.readBytes(raw);
                            raw = Utils.wrapAddress(raw, destHost, destPort, false);
                            bb.clear();
                            bb.writeBytes(raw);
                            state = State.STREAMING;
                        }
                        byte enc[] = Utils.encryptBuf(bb,encryptor);
                        bb.release();
                        bb = Utils.createFrame(enc);
                        ctx0.writeAndFlush(bb, promise);
                    }
                });
        if (!isHttps) {
            ch.pipeline()
                    .addLast("encoder", new HttpRequestEncoder())
                    .addLast("check", new ChannelOutboundHandlerAdapter() {
                        @Override
                        public void write(ChannelHandlerContext ctx0, Object msg, ChannelPromise promise) throws Exception {
                            ctx0.writeAndFlush(msg, promise);
                            ctx0.pipeline().remove("encoder");
                            ctx0.pipeline().remove("check");
                            ctx.pipeline().remove("encoder");
                            ctx.pipeline().remove("decoder");
                            ctx.pipeline().remove("aggregator");
                        }
                    });
        }
        ch.pipeline()
                //Inbound
                .addLast(new LengthFieldBasedFrameDecoder(65536,0,2,0,2))
                .addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx0, Object msg) throws Exception {
                        byte[] rqst=Utils.decryptBuf((ByteBuf)msg,encryptor);

                        //release the ByteBuf for it is not delivered to the next handler in the pipeline
                        ((ByteBuf)msg).release();
                        ByteBuf bb = ByteBufAllocator.DEFAULT.buffer();
                        bb.writeBytes(rqst);
                        ctx.writeAndFlush(bb);
                    }
                });
    }
}
