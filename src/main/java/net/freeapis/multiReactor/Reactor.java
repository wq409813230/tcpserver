package net.freeapis.multiReactor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * Created by wuqiang on 2017/8/27.
 */
public class Reactor extends Thread{

    private final Selector selector;

    private static final int SELECT_TIMEOUT = 3000;

    public Reactor() throws IOException{
        this.selector = Selector.open();
    }

    public void registerChannel(SocketChannel socketChannel) throws IOException{
        new IOWorker(selector,socketChannel);
    }

    public void run(){
        try {
            while(true){
                if(selector.select(SELECT_TIMEOUT) != 0){
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    for(SelectionKey selectionKey : selectionKeys){
                        IOWorker ioWorker = (IOWorker) selectionKey.attachment();
                        Server.executor.execute(ioWorker);
                    }
                    selectionKeys.clear();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(selector != null) selector.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
