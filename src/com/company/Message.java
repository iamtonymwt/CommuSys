package com.company;

import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;


public class Message {
    final static short TYPE_CONNECT = 0;
    final static short TYPE_CONTENT = 1;
    final static short TYPE_DISCONNECT = 2;

    private short type;
    private String to;
    private String from;
    private String content;

    public Message(short type, String to, String from, String content) {
        this.type = type;
        this.to = to;
        this.from = from;
        this.content = content;
    }

    public Message(short type, String from) {
        this.type = type;
        this.from = from;
    }


    public short getType() {
        return type;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getContent() {
        return content;
    }

    public void setType(short type) {
        this.type = type;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    public static String encode(Message message){
        String str="";
        switch (message.type){
            case TYPE_CONNECT, TYPE_DISCONNECT -> {
                str=message.getType()+"###"+message.getFrom();
            }
            case TYPE_CONTENT -> {
                str= message.getType()+"###"+message.getTo()+"###"+message.getFrom()+"###"+message.getContent();
            }
        }
        return str;
    }

    public static Message decode(String message){
        Message message1;
        StringTokenizer stringTokenizer=new StringTokenizer(message,"###");
        short type = Short.parseShort(stringTokenizer.nextToken());
        switch (type){
            case TYPE_CONNECT, TYPE_DISCONNECT -> {
                String from = stringTokenizer.nextToken();
                message1=new Message(type,from);
            }

            case TYPE_CONTENT -> {
                String to = stringTokenizer.nextToken();
                String from = stringTokenizer.nextToken();
                String content = stringTokenizer.nextToken();
                message1=new Message(type,from,to,content);
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
        return message1;
    }


    public static void main(String[] args) {
        Message m= new Message(TYPE_CONTENT,"aaaa","bbbb","cccc");
        System.out.println(Message.encode(m));
        System.out.println(decode(Message.encode(m)));

    }
}
