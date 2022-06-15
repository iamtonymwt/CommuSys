package com.company;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server_table {
    public static Map<String, SocketChannel> connected = new ConcurrentHashMap<>();

}
