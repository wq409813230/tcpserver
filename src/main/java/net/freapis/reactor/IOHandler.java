package net.freapis.reactor;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by wuqiang on 2017/6/11.
 */
public class IOHandler {

    private SocketChannel socketChannel;

    private SelectionKey selectionKey;

    private ByteBuffer readBuffer;

    private ByteBuffer writeBuffer;

    private int lastPacketPosition;//上一个完整处理的数据包在buffer中的位置

    private static final int DEFAULT_READ_BUFFER_SIZE = 100;

    private static final int DEFAULT_WRITE_BUFFER_SIZE = 1024;

    private static final char PACKET_DELIMITER = 0x0D0A;//数据包分隔符，默认为回车换行'\r\n'

    private static final long CLIENT_FORCE_CLOSE_UNIX = 0xFFF4FFFD06000000L;

    private static final long CLIENT_FORCE_CLOSE_WINDOWS = 0x0300000000000000L;

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

    private void read() throws Exception{
        socketChannel.read(readBuffer);
        int currentPosition = readBuffer.position();
        String line = null;
        //使用lastpacketPosition变量记住上次读取的数据包位置,解决数据包粘包的问题
        for(int i = lastPacketPosition; i < currentPosition; i++){
            boolean isForceClose = (
                    readBuffer.getLong(i) == CLIENT_FORCE_CLOSE_UNIX
                            || readBuffer.getLong(i) == CLIENT_FORCE_CLOSE_WINDOWS);
            if(isForceClose) socketChannel.close();
            if(readBuffer.getChar(i) == PACKET_DELIMITER){
                byte[] packet = new byte[i - lastPacketPosition];
                readBuffer.position(lastPacketPosition);
                readBuffer.get(packet);
                readBuffer.position(currentPosition);
                line = new String(packet,"UTF-8");
                lastPacketPosition = i + 2;
                break;
            }
        }

        if(line != null){
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_READ);
            writeBuffer.clear();
            writeBuffer.put(CommandUtil.runShell(line).getBytes());
            writeBuffer.flip();
            this.write();
        }
        //随时判断缓冲区使用情况,清理之前已经读取过的内存
        if(readBuffer.position() > readBuffer.capacity() / 2){
            readBuffer.limit(readBuffer.position());
            readBuffer.position(lastPacketPosition);
            readBuffer.compact();
            lastPacketPosition = 0;
        }
    }

    private void write() throws Exception{
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
}
