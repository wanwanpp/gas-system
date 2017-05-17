package com.wp.apollo.third;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

public class CallbackServer {

    private static String HOST = "tcp://localhost:61613";
    private static String USERNAME = "admin";
    private static String PASSWORD = "password";
    private final static boolean CLEAN_START = true;
    private final static short KEEP_ALIVE = 30;// 低耗网络，但是又需要及时获取数据，心跳30s  
    public static Topic[] topics = {new Topic("mqtt/fusesource/callback", QoS.EXACTLY_ONCE)};
    public final static long RECONNECTION_ATTEMPT_MAX = 6;
    public final static long RECONNECTION_DELAY = 2000;

    public final static int SEND_BUFFER_SIZE = 2 * 1024 * 1024;// 发送最大缓冲为2M  

    public static void main(String[] args) throws Exception {
        // 创建MQTT对象  
        MQTT mqtt = new MQTT();
        mqtt.setClientId("CallbackServer");
        // 设置mqtt broker的ip和端口  
        mqtt.setHost(HOST);
        mqtt.setUserName(USERNAME);
        mqtt.setPassword(PASSWORD);
        // 连接前清空会话信息  
        mqtt.setCleanSession(CLEAN_START);
        // 设置重新连接的次数  
        mqtt.setReconnectAttemptsMax(RECONNECTION_ATTEMPT_MAX);
        // 设置重连的间隔时间  
        mqtt.setReconnectDelay(RECONNECTION_DELAY);
        // 设置心跳时间  
        mqtt.setKeepAlive(KEEP_ALIVE);
        // 设置缓冲的大小  
        mqtt.setSendBufferSize(SEND_BUFFER_SIZE);
        // 获取mqtt的连接对象CallbackConnection  
        CallbackConnection connection = mqtt.callbackConnection();
        connection.connect(new Callback<Void>() {
            public void onSuccess(Void value) {
                System.out.println("连接成功:" + value);
            }

            public void onFailure(Throwable value) {
                System.out.println("连接失败");
            }
        });
        //发布消息

        for (int i = 0; i < 10; i++) {
            connection.publish("mqtt/fusesource/callback", "测试mqtt数据".getBytes(), QoS.EXACTLY_ONCE,
                    true, new Callback<Void>() {
                        public void onSuccess(Void value) {
                            //与服务器断开连接成功
                            System.out.println("发送成功:" + value);
                        }

                        public void onFailure(Throwable value) {
                            //与服务器断开连接失败
                            System.out.println("发送失败");
                        }
                    });
        }
        Thread.sleep(600000);
    }

}  