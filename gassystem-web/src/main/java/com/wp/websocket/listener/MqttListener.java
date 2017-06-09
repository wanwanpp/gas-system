package com.wp.websocket.listener;

import com.wp.api.MqttConnectionCycle;
import com.wp.protobuf.GasDataUtil;
import com.wp.protobuf.GasMsg;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;

import java.net.URISyntaxException;
import java.util.Observable;

// TODO: 2017/6/9 0009 加入观察者模式
public class MqttListener extends Observable implements MqttConnectionCycle, ApplicationListener<ApplicationPreparedEvent> {

    private Logger logger = LoggerFactory.getLogger(MqttListener.class);

    final GasDataUtil gasDataUtil = new GasDataUtil();

    final String destination = "/topic/event";

    private MQTT mqtt = new MQTT();

    private CallbackConnection connection;

    //    构造函数
    private MqttListener() {
        config("admin", "password", "localhost", 61613);
    }

    //    使用单例模式
    private static class Hoder {
        private static MqttListener singleListener = new MqttListener();
    }

    public static MqttListener getInstance() {
        return Hoder.singleListener;
    }

//    public MqttListener(String user, String password, String host, int port) {
//        config(user, password, host, port);
//    }

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
        connection.listener(new Listener() {

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


    @Override
    public void onApplicationEvent(ApplicationPreparedEvent applicationPreparedEvent) {

        MqttListener mqttListener = getInstance();
        logger.info("配置好mqttlistener");
        mqttListener.connect();
        logger.info("mqttlistener连接成功");
        mqttListener.listener();
        logger.info("mqttlistener开始监听");
    }
}
