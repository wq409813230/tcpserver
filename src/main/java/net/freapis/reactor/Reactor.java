package net.freapis.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by wuqiang on 2017/6/11.
 */
public class Reactor {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private static final int DEFAULT_PORT = 9527;

    private static final int SELECT_TIMEOUT = 3000;

    public Reactor() throws IOException{
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(DEFAULT_PORT));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("nio server started at port " + DEFAULT_PORT);
    }

    public void start(){
        try {
            while(true){
                if(selector.select(SELECT_TIMEOUT) != 0){
                    Iterator<SelectionKey> keysIter = selector.selectedKeys().iterator();
                    while (keysIter.hasNext()){
                        this.react(keysIter.next());
                        keysIter.remove();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(selector != null) selector.close();
                if(serverSocketChannel != null) serverSocketChannel.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void react(SelectionKey selectionKey) throws Exception{
        if(selectionKey.isAcceptable()){
            new Acceptor().accept();
        }else{
            ((IOHandler)selectionKey.attachment()).handle();
        }
    }

    class Acceptor{

        public void accept() throws Exception{
            SocketChannel socketChannel = serverSocketChannel.accept();
            new IOHandler(selector,socketChannel);
            System.out.println("accept new connection from channel " + socketChannel.socket().getRemoteSocketAddress());
        }
    }
}
