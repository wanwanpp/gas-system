package com.wp.websocket.listener;

import com.wp.api.MqttConnectionCycle;
import com.wp.influxdb.InfluxTemplate;
import com.wp.protobuf.GasDataUtil;
import com.wp.protobuf.GasMsg;
import com.wp.websocket.SocketHandler;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

// TODO: 2017/6/9 0009 加入观察者模式,无法加入观察者，因为SocketHandler是单例的Component
public class MqttListener extends Observable implements MqttConnectionCycle, ApplicationListener<ApplicationPreparedEvent> {

    private Logger logger = LoggerFactory.getLogger(MqttListener.class);

    final GasDataUtil gasDataUtil = new GasDataUtil();

    final String destination = "/topic/event";

    private MQTT mqtt = new MQTT();

    private CallbackConnection connection;

    //    @Autowired
    private InfluxTemplate influxTemplate = new InfluxTemplate();

    //influxdb数据库表名
    private static String tableName = "gas";


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
                List<GasMsg.GasData> gasDataList = gasDataBox.getGasDataList();
                System.out.println(gasDataBox.getGasDataList().size());

                List<WebSocketSession> sessions = SocketHandler.sessions;
                if (sessions.size() > 0) {
                    for (WebSocketSession session : sessions) {
                        try {
                            session.sendMessage(new TextMessage(gasDataList.toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                List<String> toDbList = new ArrayList<String>(100000);

                String string = "";
                for (GasMsg.GasData gasData : gasDataList) {
                    string = tableName +
                            ",id=" + gasData.getId() + " " +
                            "pressure=" + gasData.getPressure() + "," +
                            "temper=" + gasData.getTemper() + "," +
                            "sFlow=" + gasData.getSFlow() + "," +
                            "wFlow=" + gasData.getWFlow() + "," +
                            "aFlow=" + gasData.getAFlow() + "," +
                            "stime=" + gasData.getTime();//不能写成time，因为是Influxdb占用的字段
                    toDbList.add(string);
                }
                //写入数据库
                influxTemplate.write(toDbList);
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
