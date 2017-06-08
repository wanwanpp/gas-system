package com.wp.core;

import com.wp.api.MqttConnectionCycle;
import com.wp.protobuf.GasDataUtil;
import com.wp.protobuf.GasMsg;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;

import java.net.URISyntaxException;

public class MqttListener implements MqttConnectionCycle {

    final GasDataUtil gasDataUtil = new GasDataUtil();

    final String destination = "/topic/event";

    private MQTT mqtt = new MQTT();

    private CallbackConnection connection;

    public MqttListener() {
        config("admin", "password", "localhost", 61613);
    }

    public MqttListener(String user, String password, String host, int port) {
        config(user, password, host, port);
    }

    public void config(String user, String password, String host, int port) {
        try {
            mqtt.setHost(host, port);
        } catch (URISyntaxException e) {
            System.out.println("无法连接主机");
            e.printStackTrace();
        }
        mqtt.setUserName(user);
        mqtt.setPassword(password);
        connection = mqtt.callbackConnection();
    }

    public void connect() {
        connection.connect(new Callback<Void>() {
            public void onSuccess(Void value) {
                Topic[] topics = {new Topic(destination, QoS.AT_LEAST_ONCE)};

                connection.subscribe(topics, new Callback<byte[]>() {
                    public void onSuccess(byte[] qoses) {
                        System.out.println("订阅成功");
                    }

                    public void onFailure(Throwable value) {
                        value.printStackTrace();
                        System.exit(-2);
                    }
                });
            }

            public void onFailure(Throwable value) {
                value.printStackTrace();
                System.exit(-2);
            }
        });
    }

    public void listener() {
        connection.listener(new org.fusesource.mqtt.client.Listener() {

            public void onConnected() {
            }

            public void onDisconnected() {
            }

            public void onFailure(Throwable value) {
                value.printStackTrace();
                System.exit(-2);
            }

            public void onPublish(UTF8Buffer topic, Buffer msg, Runnable ack) {
                byte[] data = msg.toByteArray();
                GasMsg.GasDataBox gasDataBox = gasDataUtil.consume(data);
                System.out.println(gasDataBox.getGasDataList().size());
                for (GasMsg.GasData gasData : gasDataBox.getGasDataList()) {
                    System.out.println(gasData.toString());
                }
            }
        });

    }

    public void disconnect() {
        connection.disconnect(new Callback<Void>() {
            public void onSuccess(Void value) {
                System.out.println("收到断开指令，关闭连接");
                System.exit(0);
            }

            public void onFailure(Throwable value) {
                value.printStackTrace();
                System.exit(-2);
            }
        });
    }

    public static void main(String[] args) throws Exception {


        // Wait forever..
        synchronized (MqttListener.class) {
            while (true)
                MqttListener.class.wait();
        }
    }
}
