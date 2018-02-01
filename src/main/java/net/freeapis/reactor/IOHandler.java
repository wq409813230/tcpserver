package net.freeapis.reactor;

import net.freeapis.reactor.telnet.CommandUtil;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by wuqiang on 2017/6/11.
 */
public abstract class IOHandler {

    protected SocketChannel socketChannel;

    protected SelectionKey selectionKey;

    protected ByteBuffer readBuffer;

    protected ByteBuffer writeBuffer;

    protected int lastPacketPosition;//上一个完整处理的数据包在buffer中的位置

    private static final int DEFAULT_READ_BUFFER_SIZE = 1000;

    private static final int DEFAULT_WRITE_BUFFER_SIZE = 1024000;

    protected static final long CLIENT_FORCE_CLOSE_UNIX = 0xFFF4FFFD06000000L;

    protected static final long CLIENT_FORCE_CLOSE_WINDOWS = 0x0300000000000000L;

    public IOHandler(Selector selector,SocketChannel socketChannel) throws Exception{
        this.socketChannel = socketChannel;
        this.socketChannel.configureBlocking(false);
        this.selectionKey = socketChannel.register(selector,0);
        this.selectionKey.interestOps(SelectionKey.OP_READ);
        this.selectionKey.attach(this);
        this.readBuffer = ByteBuffer.allocateDirect(DEFAULT_READ_BUFFER_SIZE);
        this.writeBuffer = ByteBuffer.allocateDirect(DEFAULT_WRITE_BUFFER_SIZE);
        this.writeBuffer.put("Welcome to Simple NIO server implemented with Reactor!".getBytes());
        this.writeBuffer.flip();
        this.write();
    }

    public void handle() throws Exception{
        if(selectionKey.isReadable()){
            this.read();
        }else if(selectionKey.isWritable()){
            write();
        }
    }

    protected abstract void read() throws Exception;

    protected void write() throws Exception{
        if(!socketChannel.isConnected())
            throw new Exception("socket has closed!");
        int written = socketChannel.write(writeBuffer);
        System.out.println("has written : " + written + " bytes");

        if(writeBuffer.hasRemaining()){
            System.out.println("write not finished,remain : " + writeBuffer.remaining() + " bytes");
            writeBuffer.compact();
            writeBuffer.flip();
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
        }else{
            writeBuffer.clear();
            selectionKey.interestOps(~SelectionKey.OP_WRITE & selectionKey.interestOps() | SelectionKey.OP_READ);
        }
    }

    protected void clearUnusedMemory(){
        if(readBuffer.position() > readBuffer.capacity() / 2){
            readBuffer.limit(readBuffer.position());
            readBuffer.position(lastPacketPosition);
            readBuffer.compact();
            lastPacketPosition = 0;
        }
    }
}
