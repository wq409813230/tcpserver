package net.freeapis.multiReactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wuqiang on 2017/8/27.
 */
public class Server {

    private final ServerSocketChannel serverSocketChannel;
    public static final ExecutorService executor = Executors.newCachedThreadPool();
    private final Reactor[] reactors;

    public Server(int port) throws IOException{
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.configureBlocking(true);
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        reactors = new Reactor[1];//Runtime.getRuntime().availableProcessors()
        for(int i = 0;i < reactors.length; i++){
            reactors[i] = new Reactor();
            reactors[i].start();
        }
        System.out.println("NIO Server started at port: " + port);
    }

    public void start() throws IOException{
        int connectionIndex = 0;
        while (true){
            SocketChannel socketChannel = this.serverSocketChannel.accept();
            System.out.println("established connection from remote address : " + socketChannel.socket().getRemoteSocketAddress());
            reactors[connectionIndex++ % reactors.length].registerChannel(socketChannel);
        }
    }

    public static void main(String[] args) throws Exception{
        new Server(9527).start();
    }
}
