package com.wp.core;

/**
 * Created by 王萍 on 2017/5/24 0024.
 */
public class Main {



    public static void main(String[] args) throws Exception {

        ApolloReceiver receiver = new ApolloReceiver("admin", "password", "localhost", 61613);
        receiver.setDestination("/topic/event");

        receiver.start();

        // Wait forever..
        synchronized (Listener.class) {
            while (true)
                Listener.class.wait();
        }
    }
}
