package com.wp.core;

import com.wp.protobuf.BuildGasData;
import com.wp.protobuf.GasMsg;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 王萍 on 2017/5/23 0023.
 */
public class ApolloReceiver {

    final BuildGasData buildGasData = new BuildGasData();

    private static final int DEFAULT_NUM_CONSUMERS = 5;

    private int numConsumers = DEFAULT_NUM_CONSUMERS;

    private String destination;

    private List<Consumer> consumers = new ArrayList<Consumer>();

    public int getNumConsumers() {
        return numConsumers;
    }

    public void setNumConsumers(int numConsumers) {
        this.numConsumers = numConsumers;
    }

    private ExecutorService consumersPool;

    private static MQTT mqtt = new MQTT();

    private static CallbackConnection connection;

    public ApolloReceiver(String user, String password, String host, int port) throws URISyntaxException {

        mqtt.setHost(host, port);
        mqtt.setUserName(user);
        mqtt.setPassword(password);
        connection = mqtt.callbackConnection();
    }

    public void start() {
        startConsumers();
    }

    protected void startConsumers() {

        //清空消费者集合
        consumers.clear();

        //初始化消费者线程池，自定义的线程创建工厂类
        consumersPool = Executors.newFixedThreadPool(getNumConsumers(), new ConsumersThreadFactory());

        for (int i = 0; i < getNumConsumers(); i++) {
            Consumer consumer = new Consumer();
            consumer.connect();
            consumersPool.execute(consumer);
            consumers.add(consumer);
        }
    }

    public void stop() {
        stopConsumers();
    }


    protected void stopConsumers() {
        //关闭线程池，但是和shutdown()方法一样，不一定能马上关闭。shutdown方法会等待所有任务执行完了再关闭。
        consumersPool.shutdownNow();
        for (Consumer consumer : consumers) {
            consumer.disconnect();
        }
    }

    private class ConsumersThreadFactory implements ThreadFactory {

        private AtomicInteger counter = new AtomicInteger();

        public Thread newThread(Runnable r) {
            return new Thread(r, "Consumer " + counter.incrementAndGet());
        }
    }

    private class Consumer implements Runnable {

        private void disconnect() {
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

        public void run() {
            connection.listener(new org.fusesource.mqtt.client.Listener() {
                long count = 0;
                long start = System.currentTimeMillis();

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
                    GasMsg.GasDataBox gasDataBox = buildGasData.consume(data);
                    System.out.println(Thread.currentThread().getName() + " receive: " + gasDataBox.getGasDataList().size());

//                    if (count == 3) {
//                        long diff = System.currentTimeMillis() - start;
//                        System.out.println(String.format("Received %d in %.2f seconds", count, (1.0 * diff / 1000.0)));
//                        disconnect();
//                    } else {
//                        if (count == 0) {
//                            start = System.currentTimeMillis();
//                        }
//                        if (count % 1000 == 0) {
//                            System.out.println(String.format("Received %d messages.", count));
//                        }
//                        count++;
//                    }
                }
            });
        }

    }


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

    public void setDestination(String destination) {
        this.destination = destination;
    }

}
