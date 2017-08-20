package net.freeapis.reactor.tcp;

import net.freeapis.reactor.IOHandler;
import net.freeapis.util.ByteKit;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by wuqiang on 2017/8/20.
 */
public class TCPHandler extends IOHandler{

    private static final Long PACKET_DELIMITER = 0x0301040105090206L;

    public TCPHandler(Selector selector, SocketChannel socketChannel) throws Exception {
        super(selector, socketChannel);
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
}
