package com.bendb.thrifty.transport;

import okio.Okio;
import okio.Sink;
import okio.Source;

import javax.net.SocketFactory;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TSocket implements Closeable {
    private final String host;
    private final int port;
    private final int readTimeout;
    private final int connectTimeout;
    private final SocketFactory socketFactory;

    private Socket socket;
    private Source source;
    private Sink sink;

    public static class Builder {
        private final String host;
        private final int port;
        private int readTimeout;
        private int connectTimeout;
        private SocketFactory socketFactory;

        public Builder(String host, int port) {
            if (host == null || host.length() == 0) {
                throw new NullPointerException("host");
            }

            if (port < 0 || port > 0xFFFF) {
                throw new IllegalStateException("Invalid port number: " + port);
            }

            this.host = host;
            this.port = port;
        }

        public Builder readTimeout(int readTimeout) {
            if (readTimeout < 0) {
                throw new IllegalArgumentException("readTimeout cannot be negative");
            }
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            if (connectTimeout < 0) {
                throw new IllegalArgumentException("connectTimeout cannot be negative");
            }
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder socketFactory(SocketFactory socketFactory) {
            if (socketFactory == null) {
                throw new NullPointerException("socketFactory");
            }

            this.socketFactory = socketFactory;
            return this;
        }

        public TSocket build() {
            return new TSocket(this);
        }
    }

    TSocket(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.readTimeout = builder.readTimeout;
        this.connectTimeout = builder.connectTimeout;
        this.socketFactory = builder.socketFactory == null
                ? SocketFactory.getDefault()
                : builder.socketFactory;
    }

    public boolean isConnected() {
        Socket s = socket;
        return s != null && s.isConnected() && !s.isClosed();
    }

    public Source getSource() {
        return source;
    }

    public Sink getSink() {
        return sink;
    }

    public void connect() throws IOException {
        if (socket == null) {
            socket = socketFactory.createSocket();
        }

        socket.setTcpNoDelay(true);
        socket.setSoLinger(false, 0);
        socket.setKeepAlive(true);
        socket.setSoTimeout(readTimeout);

        socket.connect(InetSocketAddress.createUnresolved(host, port), connectTimeout);

        source = Okio.source(socket);
        sink = Okio.sink(socket);
    }

    @Override
    public void close() {
        Socket socket = this.socket;
        Source source = this.source;
        Sink sink = this.sink;

        this.socket = null;
        this.source = null;
        this.sink = null;

        if (source != null) {
            try {
                source.close();
            } catch (IOException ignored) {}
        }

        if (sink != null) {
            try {
                sink.close();
            } catch (IOException ignored) {}
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

}
