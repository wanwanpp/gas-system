package com.wp.core;

import com.wp.protobuf.GasDataUtil;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

/**
 * Created by 王萍 on 2017/5/22 0022.
 */
public class Publisher {

    public static void main(String[] args) throws Exception {

        GasDataUtil gasDataUtil = new GasDataUtil();

        //基本参数
        String user = "admin";
        String password = "password";
        String host = "localhost";
        int port = 61613;
        final String destination = "/topic/event";

        //配置MQTT对象
        MQTT mqtt = new MQTT();
        mqtt.setHost(host, port);
        mqtt.setUserName(user);
        mqtt.setPassword(password);

        //获取Future连接
        FutureConnection connection = mqtt.futureConnection();
        //连接队列服务器
        connection.connect().await();

        long start = System.currentTimeMillis();

        //获取模拟数据
        byte[] dataBytes = gasDataUtil.produceGasData();

        for (int i = 0; i < 1; i++) {
            //发送数据
            connection.publish(destination, dataBytes, QoS.AT_LEAST_ONCE, true);
            Thread.sleep(500);
        }

        //关闭连接
        connection.disconnect().await();
        System.out.println("used :" + (System.currentTimeMillis() - start));
//        Thread.sleep(600000);

    }
}
