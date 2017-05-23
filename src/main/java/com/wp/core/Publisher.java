package com.wp.core;

import com.wp.protobuf.BuildGasData;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import java.util.LinkedList;

/**
 * Created by 王萍 on 2017/5/22 0022.
 */
public class Publisher {

    public static void main(String[] args) throws Exception {

        BuildGasData buildGasData = new BuildGasData();

        String user = "admin";
        String password = "password";
        String host = "localhost";
        int port = 61613;

        //默认目的地为/topic/event
        final String destination = "/topic/event";

        MQTT mqtt = new MQTT();
        mqtt.setHost(host, port);
        mqtt.setUserName(user);
        mqtt.setPassword(password);

        FutureConnection connection = mqtt.futureConnection();
        connection.connect().await();

        final LinkedList<Future<Void>> queue = new LinkedList<Future<Void>>();
        UTF8Buffer topic = new UTF8Buffer(destination);

        int count = 0;
        long start = System.currentTimeMillis();
        byte[] dataBytes = buildGasData.produceGasData();
        System.out.println(dataBytes.length);

        for (int i = 0; i < 100; i++) {
            queue.add(connection.publish(destination, dataBytes, QoS.AT_LEAST_ONCE, true));
            Thread.sleep(500);
        }

        System.out.println("count is " + count);

        connection.disconnect().await();
        System.out.println("used :" + (System.currentTimeMillis() - start));
        Thread.sleep(600000);

    }
}
