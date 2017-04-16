package net.freeapis.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.util.Iterator;

/**
 * freeapis, Inc.
 * Copyright (C): 2016
 * <p>
 * Description:
 * TODO
 * <p>
 * Author:Administrator
 * Date:2017年04月14日 11:19
 */
public class TelnetServer {

    private static final int TCP_PORT = 9527;

    private static final int SELECT_TIMEOUT = 3000;

    private static final int DEFAULT_BUFFER_SIZE = 128;

    public static void main(String[] args) {
        Selector selector = null;
        ServerSocketChannel serverSocketChannel = null;

        try{
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(TCP_PORT));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while(true){
                if(selector.select(SELECT_TIMEOUT) != 0){
                    Iterator<SelectionKey> keysIter = selector.selectedKeys().iterator();
                    while (keysIter.hasNext()){
                        SelectionKey key = keysIter.next();
                        if(key.isAcceptable()){
                            accept(key);
                        }
                        if(key.isReadable()){
                            read(key);
                        }
                        if(key.isConnectable()){
                            System.out.println("connected");
                        }
                        keysIter.remove();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally{
            try {
                if(selector != null) selector.close();
                if(serverSocketChannel != null) serverSocketChannel.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private static void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        ByteBuffer channelBuffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
        socketChannel.register(key.selector(),SelectionKey.OP_READ, channelBuffer);

        channelBuffer.put("welcome to simple telnet server!".getBytes());
        channelBuffer.flip();
        socketChannel.write(channelBuffer);
        channelBuffer.clear();
    }

    private static void read(SelectionKey key) throws IOException{
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer channelBuffer = (ByteBuffer)key.attachment();
        /*while(socketChannel.read(channelBuffer) > 0){
            channelBuffer.flip();
            socketChannel.write(channelBuffer);
            channelBuffer.clear();
        }*/

        socketChannel.read(channelBuffer);

        int platformBufferSize = socketChannel.socket().getSendBufferSize();
        channelBuffer = ByteBuffer.allocate(platformBufferSize * 5);
        for(int i = 0; i < channelBuffer.capacity(); i++){
            channelBuffer.put((byte)(65 + i % 25));
        }
        channelBuffer.flip();
        System.out.println("big net pkg,length : " + channelBuffer.capacity() + "bytes");

        int written = socketChannel.write(channelBuffer);
        System.out.println("has written : " + written + "bytes");

        if(channelBuffer.hasRemaining()){
            System.out.println("write not finished,remain : " + channelBuffer.remaining());
        }
    }
}
