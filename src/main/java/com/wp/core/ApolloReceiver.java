package com.wp.core;

import com.wp.influxdb.InfluxTemplate;
import com.wp.protobuf.GasDataUtil;
import com.wp.protobuf.GasMsg;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 王萍 on 2017/5/23 0023.
 */
public class ApolloReceiver {

    //写入数据表名
    private static String tableName = "gas";

    //产生模拟数据，并使用protobuf编解码工具类
    final GasDataUtil gasDataUtil = new GasDataUtil();

    //consumer数量
    private static final int DEFAULT_NUM_CONSUMERS = 5;

    private int numConsumers = DEFAULT_NUM_CONSUMERS;

    //统一管理consumer的容器
    private List<Consumer> consumers = new ArrayList<Consumer>();

    //consumer线程池
    private ExecutorService consumersPool;

    private static MQTT mqtt = new MQTT();

    //influxdb工具类
    private static InfluxTemplate influxTemplate = new InfluxTemplate();

    //mqtt回调连接
    private CallbackConnection connection;

    //装入获取的publisher发送的数据，共享给多线程
    private LinkedBlockingQueue blockingQueue;

    //topic地址
    private String destination;

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getNumConsumers() {
        return numConsumers;
    }

    public ApolloReceiver(String user, String password, String host, int port) throws URISyntaxException {

        mqtt.setHost(host, port);
        mqtt.setUserName(user);
        mqtt.setPassword(password);
        connection = mqtt.callbackConnection();
        blockingQueue = new LinkedBlockingQueue(numConsumers);

    }

    //receive开始
    public void start() {
        startConsumers();
    }

    protected void startConsumers() {

        //清空消费者集合
        consumers.clear();

        //初始化线程池
        consumersPool = Executors.newFixedThreadPool(getNumConsumers());

        //连接
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
                System.out.println("订阅失败。");
                value.printStackTrace();
                System.exit(-2);
            }
        });

        //监听
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

                try {
                    blockingQueue.put(data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (AtomicInteger i = new AtomicInteger(1); i.get() <= getNumConsumers(); i.getAndIncrement()) {
                    Consumer consumer = new Consumer("consumer " + i);
                    consumersPool.execute(consumer);
                    consumers.add(consumer);
                }
            }
        });
    }

    //    receive关闭
    public void stop() {
        stopConsumers();
    }

    protected void stopConsumers() {

        //关闭线程池，但是和shutdown()方法一样，不一定能马上关闭。shutdown方法会等待所有任务执行完了再关闭。
        consumersPool.shutdownNow();

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

    private class Consumer implements Runnable {

        private String name;

        //消费的数据，从共享队列中获取
        private byte[] data;

        public Consumer(String name) {
            this.name = name;
        }

        public void run() {

            while (true) {

                try {
                    data = (byte[]) blockingQueue.take();
                } catch (InterruptedException e) {
                    System.out.println("从队列中获取数据失败");
                    e.printStackTrace();
                }

                //反序列化数组为GasData对象
                GasMsg.GasDataBox gasDataBox = gasDataUtil.consume(data);
                List<GasMsg.GasData> gasDataList = gasDataBox.getGasDataList();
                System.out.println(name + " receive: " + gasDataList.size());

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

        }
    }


}


