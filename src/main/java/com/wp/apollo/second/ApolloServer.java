package com.wp.apollo.second;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class ApolloServer {  
  
//    private static String host = "tcp://172.23.253.30:61613";
    private static String host = "tcp://localhost:61613";
    private static String userName = "admin";
    private static String passWord = "password";  
    private static MqttClient client;  
    private static MqttTopic topic;  
    private static MqttMessage message;  
  
    private static String topicStr = "mqtt/topic";  
  
    public static void main(String[] args) throws MqttException {  
        // host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，  
        // MemoryPersistence设置clientid的保存形式，默认为以内存保存  
        client = new MqttClient(host, "CallbackServer", new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();  
        // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，  
        // 这里设置为true表示每次连接到服务器都以新的身份连接  
        options.setCleanSession(true);  
        // 设置连接的用户名  
        options.setUserName(userName);  
        // 设置连接的密码  
        options.setPassword(passWord.toCharArray());  
        // 设置超时时间 单位为秒  
        options.setConnectionTimeout(10);  
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制  
        options.setKeepAliveInterval(20);  
        client.setCallback(new MqttCallback() {  
            public void messageArrived(MqttTopic topicName, MqttMessage message) throws Exception {  
                 //subscribe后得到的消息会执行到这里面    
                System.out.println("messageArrived----------");    
                System.out.println(topicName+"---"+message.toString());    
            }  
            public void deliveryComplete(MqttDeliveryToken token) {  
                 //publish后会执行到这里    
                System.out.println("deliveryComplete---------"    
                        + token.isComplete());    
            }  
            public void connectionLost(Throwable cause) {  
                // //连接丢失后，一般在这里面进行重连    
                 System.out.println("connectionLost----------");    
            }

            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                System.out.println("mqttMessage is "+mqttMessage.toString()+"  s is: "+s);
            }

            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }

        });
         topic = client.getTopic(topicStr);  
         message = new MqttMessage();    
         message.setQos(1);    
         message.setRetained(true);    
         System.out.println(message.isRetained()+"------ratained状态");    
         message.setPayload("mqtt.....test....".getBytes());    
         client.connect(options);    
         MqttDeliveryToken token = topic.publish(message);    
         token.waitForCompletion();    
         System.out.println(token.isComplete()+"========");    
    }  
}  