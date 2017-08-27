package net.freeapis.multiReactor;

import net.freeapis.util.ByteKit;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by wuqiang on 2017/8/27.
 */
public class IOWorker implements Runnable{

    protected SocketChannel socketChannel;

    protected SelectionKey selectionKey;

    protected ByteBuffer readBuffer;

    protected ByteBuffer writeBuffer;

    protected int lastPacketPosition;//上一个完整处理的数据包在buffer中的位置

    private static final int DEFAULT_READ_BUFFER_SIZE = 1000;

    private static final int DEFAULT_WRITE_BUFFER_SIZE = 1000;

    protected static final long CLIENT_FORCE_CLOSE_UNIX = 0xFFF4FFFD06000000L;

    protected static final long CLIENT_FORCE_CLOSE_WINDOWS = 0x0300000000000000L;

    private static final Long PACKET_DELIMITER = 0x0301040105090206L;

    public IOWorker(Selector selector, SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
        this.socketChannel.configureBlocking(false);
        this.selectionKey = socketChannel.register(selector,SelectionKey.OP_READ);
        this.selectionKey.attach(this);
        this.readBuffer = ByteBuffer.allocateDirect(DEFAULT_READ_BUFFER_SIZE);
        this.writeBuffer = ByteBuffer.allocateDirect(DEFAULT_WRITE_BUFFER_SIZE);
        this.writeBuffer.put("Welcome to Simple NIO server implemented with Reactor!".getBytes());
        this.writeBuffer.flip();
        this.write();
    }

    protected void read() throws Exception {
        socketChannel.read(readBuffer);
        int currentPosition = readBuffer.position();
        byte[] packet = null;
        //使用lastpacketPosition变量记住上次读取的数据包位置,解决数据包粘包的问题
        for(int i = lastPacketPosition; i < currentPosition; i++){
            boolean isForceClose = (
                    readBuffer.getLong(i) == CLIENT_FORCE_CLOSE_UNIX
                            || readBuffer.getLong(i) == CLIENT_FORCE_CLOSE_WINDOWS);
            if(isForceClose) socketChannel.close();
            if(readBuffer.getLong(i) == PACKET_DELIMITER){
                packet = new byte[i - lastPacketPosition];
                readBuffer.position(lastPacketPosition);
                readBuffer.get(packet);
                lastPacketPosition = i + 8;
                break;
            }
        }

        if(packet != null){
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_READ);
            writeBuffer.clear();
            writeBuffer.put(ByteKit.toHexString(packet,true).getBytes());
            writeBuffer.flip();
            this.write();
        }
        //随时判断缓冲区使用情况,清理之前已经读取过的内存
        clearUnusedMemory();
    }

    protected void write() throws IOException{
        if(!socketChannel.isConnected())
            throw new IOException("socket has closed!");
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

    public void run() {
        try{
            if(selectionKey.isReadable()){
                this.read();
            }else if(selectionKey.isWritable()){
                this.write();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
