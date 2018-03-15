package proxy.server;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import proxy.handler.ServerHandler;

public class HTTPServer extends AbstractServer {

    public HTTPServer(int port, int timeout, int backlog) throws Exception {
        super(port,timeout,backlog);
    }

    @Override
    protected ChannelInitializer getHandler(){
        return new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel sc) {
                sc.pipeline()
                        //HTTPServerCodec = RequestDecoder + ResponseEncoder
                        .addLast("encoder", new HttpResponseEncoder())
                        .addLast("decoder", new HttpRequestDecoder())
                        //HttpObjectAggregator produce FullHttpRequest or FullHttpResponse
                        .addLast("aggregator", new HttpObjectAggregator(65536))
                        .addLast(new ServerHandler());
            }
        };
    }

}

