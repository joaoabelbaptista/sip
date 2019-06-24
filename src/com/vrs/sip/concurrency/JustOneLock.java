package com.vrs.sip.concurrency;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import java.net.*;
import java.io.*;

public class JustOneLock {
    FileLock lock;
    FileChannel channel;
    ServerSocket socket = null;

    public boolean isAppActiveBackup() throws Exception{
        File file1 = new File(System.getProperty("user.home"),
                "FireZeMissiles1111" + ".tmp");

        File file = new File("./",
                "FireZeMissiles1111" + ".tmp");

        channel = new RandomAccessFile(file, "rw").getChannel();

        System.out.println("cenas = " + System.getProperty("user.home"));
        lock = channel.tryLock();
        if (lock == null) {

            System.out.println("null = " + System.getProperty("user.home"));

            return true;
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    lock.release();
                    channel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return false;
    }

    public boolean isAppActive(int port) throws Exception{

        port = 12345 ;

        try {
              socket = new ServerSocket(port, 10, InetAddress.getLocalHost());

        }
        catch (IOException ex) {

            return true;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

       return false;
    }

}