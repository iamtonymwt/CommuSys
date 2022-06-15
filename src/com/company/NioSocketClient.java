package com.company;

//客户端client类

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Scanner;


public class NioSocketClient {
    private String name ="mwt";
    private Queue<String> Input_cache=new ArrayDeque<>();
    public Scanner scanner = new Scanner(System.in);
    private volatile byte flag = 1;
    public NioSocketClient(String name) {
        this.name = name;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public void start() {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            //连接服务端socket
            SocketAddress socketAddress = new InetSocketAddress("localhost", 8888);
            socketChannel.connect(socketAddress);
            socketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
            System.out.println("客户端开始工作：");
            ClientHandlerBs handler= new ClientHandlerImpl(name);
            new Thread(()->{
                while (scanner.hasNext()){
                    String request =  scanner.nextLine();
                    Input_cache.add(request);
                }

            }).start();
            while(flag==1){
                selector.select();
                //获取selectionKeys并处理
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    try {
                        //读请求
                        if (key.isReadable()) {
                            System.out.println(handler.handleRead(key));
                        }
                        if(key.isWritable()) {
                            handler.handleWrite(key,Input_cache);
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    //处理完后移除当前使用的key
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new NioSocketClient(args[0]).start();
    }
}