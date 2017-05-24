/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wp.core;

import com.wp.protobuf.BuildGasData;
import com.wp.protobuf.GasMsg;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;

/**
 * Uses an callback based interface to MQTT.  Callback based interfaces
 * are harder to use but are slightly more efficient.
 */
public class Listener {

    public static void main(String[] args) throws Exception {

        final BuildGasData buildGasData = new BuildGasData();

        String user = "admin";
        String password = "password";
        String host = "localhost";

        int port = 61613;
        final String destination = "/topic/event";

        MQTT mqtt = new MQTT();
        mqtt.setHost(host, port);
        mqtt.setUserName(user);
        mqtt.setPassword(password);

        final CallbackConnection connection = mqtt.callbackConnection();

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
                System.out.println(gasDataBox.getGasDataList().size());
                for (GasMsg.GasData gasData:gasDataBox.getGasDataList()){
                    System.out.println(gasData.toString());
                }
//
//                String body = msg.utf8().toString();
//                System.out.println(body);
//                System.out.println(count);
//                if ("SHUTDOWN".equals(body)) {
//                    long diff = System.currentTimeMillis() - start;
//                    System.out.println(String.format("Received %d in %.2f seconds", count, (1.0 * diff / 1000.0)));
//                    connection.disconnect(new Callback<Void>() {
//                        public void onSuccess(Void value) {
//                            System.out.println("收到断开指令，关闭连接");
//                            System.exit(0);
//                        }
//
//                        public void onFailure(Throwable value) {
//                            value.printStackTrace();
//                            System.exit(-2);
//                        }
//                    });
//                } else {
//                    if (count == 0) {
//                        start = System.currentTimeMillis();
//                    }
//                    if (count % 1000 == 0) {
//                        System.out.println(String.format("Received %d messages.", count));
//                    }
//                    count++;
//                }
            }
        });


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

        // Wait forever..
        synchronized (Listener.class) {
            while (true)
                Listener.class.wait();
        }
    }
}
