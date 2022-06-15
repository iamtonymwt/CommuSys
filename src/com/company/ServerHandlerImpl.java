package com.company;//对selectionKey事件的处理

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;


interface ServerHandlerBs {
    void handleAccept(SelectionKey selectionKey) throws IOException;

    String handleRead(SelectionKey selectionKey) throws IOException;
}



public class ServerHandlerImpl implements ServerHandlerBs {
    private int bufferSize = 1024;
    private String localCharset = "UTF-8";

    public ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
    public ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    public ServerHandlerImpl() {
    }

    public ServerHandlerImpl(int bufferSize) {
        this(bufferSize, null);
    }

    public ServerHandlerImpl(String localCharset) {
        this(-1, localCharset);
    }

    public ServerHandlerImpl(int bufferSize, String localCharset) {
        this.bufferSize = bufferSize > 0 ? bufferSize : this.bufferSize;
        this.localCharset = localCharset == null ? this.localCharset : localCharset;
    }

    @Override
    public void handleAccept(SelectionKey selectionKey) throws IOException {
        //获取channel
        SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
        //非阻塞
        socketChannel.configureBlocking(false);
        //注册selector
        socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);

        System.out.println("建立请求......");
    }

    @Override
    public String handleRead(SelectionKey selectionKey) throws IOException {

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();


        String receivedStr = "";
        
        String returnStr = null;
        
        Message message = null;

        readBuffer.clear();
        if (socketChannel.read(readBuffer) == -1) {
            //没读到内容关闭
            socketChannel.shutdownOutput();
            socketChannel.shutdownInput();
            socketChannel.close();
            System.out.println("连接断开......");
        } else {
            //将channel改为读取状态
            readBuffer.flip();
            //按照编码读取数据
            receivedStr = Charset.forName(localCharset).newDecoder().decode(readBuffer).toString();
            System.out.println(receivedStr);
            message = Message.decode(receivedStr);
            switch (message.getType()){
                case Message.TYPE_CONNECT -> {
                    returnStr=message.getFrom()+"连接";
                    Server_table.connected.putIfAbsent(message.getFrom(),socketChannel);
                }
                
                case Message.TYPE_CONTENT -> {
                    returnStr=message.getFrom()+":"+message.getContent()+"   to:"+message.getTo();
                    if (Server_table.connected.containsKey(message.getTo())){
                        SocketChannel socketChannel1 = Server_table.connected.get(message.getTo());
                        writeBuffer.clear();
                        writeBuffer.put(receivedStr.getBytes(StandardCharsets.UTF_8));
                        writeBuffer.flip();
                        socketChannel1.write(writeBuffer);
                    }
                    else if(!Objects.equals(message.getTo(), "server")){
                        String string = "用户"+message.getTo()+"不存在";
                        Message message1=new Message(Message.TYPE_CONTENT,"server"
                                ,message.getFrom(),string);
                        writeBuffer.clear();
                        writeBuffer.put(Message.encode(message1).getBytes(StandardCharsets.UTF_8));
                        writeBuffer.flip();
                        socketChannel.write(writeBuffer);
                    }
                }
                case Message.TYPE_DISCONNECT -> {
                    socketChannel.shutdownOutput();
                    socketChannel.shutdownInput();
                    socketChannel.close();
                    System.out.println("连接断开......");
                    returnStr=message.getFrom()+"断开";
                    Server_table.connected.remove(message.getFrom());
                    return returnStr;

                }
            }
            
            socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);

        }
        
        return returnStr;
    }

}
