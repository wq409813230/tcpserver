package net.freeapis.reactor.telnet;

import net.freeapis.reactor.IOHandler;

import java.io.File;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by wuqiang on 2017/8/20.
 */
public class TelnetHandler extends IOHandler{

    private static final char PACKET_DELIMITER = 0x0D0A;//数据包分隔符，默认为回车换行'\r\n'

    public TelnetHandler(Selector selector, SocketChannel socketChannel) throws Exception {
        super(selector, socketChannel);
    }

    protected void read() throws Exception{
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

        if(line != null && !line.isEmpty()){
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_READ);
            writeBuffer.clear();
            writeBuffer.put(CommandUtil.runShell(new File("C://"),line).getBytes());
            writeBuffer.flip();
            this.write();
        }
        //随时判断缓冲区使用情况,清理之前已经读取过的内存
        clearUnusedMemory();
    }
}
