package com.company;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Queue;
import java.util.Scanner;

interface ClientHandlerBs {
    String handleRead(SelectionKey selectionKey) throws IOException;

    void handleWrite(SelectionKey selectionKey, Queue<String> Input_cache) throws IOException, InterruptedException;

}


public class ClientHandlerImpl implements ClientHandlerBs{
    private boolean send_name = false;
    private String from;
    private String to = "server";
    public ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
    public ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    public ClientHandlerImpl() {
    }

    public ClientHandlerImpl(String from) {
        this.from = from;
    }


    @Override
    public String handleRead(SelectionKey selectionKey) throws IOException {
        //System.out.println("read");

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
            return "与服务器断开连接　";
        } else {
            //将channel改为读取状态
            readBuffer.flip();
            //按照编码读取数据
            receivedStr = StandardCharsets.UTF_8.newDecoder().decode(readBuffer).toString();
            message = Message.decode(receivedStr);
            returnStr=message.getFrom()+":"+message.getContent()+"   to:"+message.getTo();

        }
        return returnStr;
    }

    @Override
    public void handleWrite(SelectionKey selectionKey,Queue<String> Input_cache) throws IOException, InterruptedException {

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        if(!send_name){
            Message message =new Message(Message.TYPE_CONNECT,from);
            writeBuffer.clear();
            writeBuffer.put(Message.encode(message).getBytes(StandardCharsets.UTF_8));
            writeBuffer.flip();
            socketChannel.write(writeBuffer);
            send_name = true;
            return;
        }

        if(Input_cache.isEmpty()){
            Thread.sleep(1000);
            return;
        }
        String request= Input_cache.poll();

        //如果有数据，则发送，且数据不为空
        if(Objects.equals(request, "exit")){
            Message message =new Message(Message.TYPE_DISCONNECT,from);
            writeBuffer.clear();
            writeBuffer.put(Message.encode(message).getBytes(StandardCharsets.UTF_8));
            writeBuffer.flip();
            socketChannel.write(writeBuffer);
            socketChannel.shutdownOutput();
            socketChannel.shutdownInput();
            socketChannel.close();
            System.out.println("连接断开......");
            return;
        }
        if(request.startsWith("to/")){
            to=request.substring(3);
            return;
        }
        if(Objects.equals(to, null)){
            System.out.println("未指定发送消息接受者");
            return;
        }
        if(request.length() > 0){
            Message message =new Message(Message.TYPE_CONTENT,from,to,request);
            writeBuffer.clear();
            writeBuffer.put(Message.encode(message).getBytes(StandardCharsets.UTF_8));
            //System.out.println(Message.encode(message));
            writeBuffer.flip();
            socketChannel.write(writeBuffer);
        }


    }

    public static void main(String[] args) {
        String s= "to/adssf";
        System.out.println(s.substring(3));
    }
}
