package com.company;//服务端server类

import com.company.ServerHandlerBs;
import com.company.ServerHandlerImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;


public class NioSocketServer {

    private volatile byte flag = 1;
    public Scanner scanner = new Scanner(System.in);


    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public void start() {
        //创建serverSocketChannel，监听8888端口
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.socket().bind(new InetSocketAddress(8888));
            //设置为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            //为serverChannel注册selector
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("服务端开始工作：");
            //创建消息处理器
            ServerHandlerBs handler = new ServerHandlerImpl(1024);
            new Thread(()->{
                while (scanner.hasNext()){
                    String request =  scanner.nextLine();
                    if(Objects.equals(request, "client")){
                        for (String i:
                             Server_table.connected.keySet()) {
                            System.out.println(i);
                        }
                    }
                }

            }).start();
            while (flag == 1) {
                selector.select();
                System.out.println("开始处理请求 ： ");
                //获取selectionKeys并处理
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    try {
                        //连接请求
                        if (key.isAcceptable()) {
                            handler.handleAccept(key);
                        }
                        //读请求
                        if (key.isReadable()) {
                            System.out.println(handler.handleRead(key));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //处理完后移除当前使用的key
                    keyIterator.remove();
                }
                System.out.println("完成请求处理。");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        NioSocketServer server = new NioSocketServer();
        new Thread(() -> {
            try {
                Thread.sleep(10*60*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                server.setFlag((byte) 0);
            }
        }).start();
        server.start();
    }
}


