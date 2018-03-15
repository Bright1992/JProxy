package proxy.client;

import proxy.AbstractProxy;
import io.netty.channel.ChannelInitializer;

public abstract class AbstractClient implements AbstractProxy {
    int localPort;
    int remotePort;
    int backlog;
    int timeout;
    String remoteHost;

    public AbstractClient(int localPort, String remoteHost, int remotePort, int timeout, int backlog) throws Exception {
        this.localPort = localPort;
        this.timeout = timeout;
        this.backlog=backlog;
        this.remotePort=remotePort;
        this.remoteHost=remoteHost;
    }

    public AbstractClient setLocalPort(int localPort) {
        this.localPort = localPort;
        return this;
    }

    public AbstractClient setBacklog(int backlog) {
        this.backlog = backlog;
        return this;
    }

    public AbstractClient setRemotePort(int remotePort) {
        this.remotePort = remotePort;
        return this;
    }

    public AbstractClient setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public abstract ChannelInitializer getHandler();
}
