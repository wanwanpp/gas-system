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
package com.wp.apollo.first;

import org.fusesource.hawtbuf.AsciiBuffer;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import java.util.LinkedList;

class Publisher {
    public static void main(String[] args) throws Exception {

        String user = "admin";
        String password = "password";
        String host ="localhost";
        int port = 61613;

        //默认目的地为/topic/event
        final String destination = "/topic/event";
        String body = "I=1,P=30,T=30,S=40,W=39,A=50";
        Buffer msg = new AsciiBuffer(body);

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
        for (int i = 1; i <= 100000; i++) {

            queue.add(connection.publish(topic, msg, QoS.AT_LEAST_ONCE, false));

            // Eventually we start waiting for old publish futures to complete
            // so that we don't create a large in memory buffer of outgoing message.s
            if (queue.size() >= 10000) {
                queue.removeFirst().await();
            }
            count++;
        }

        System.out.println("count is " + count);
        queue.add(connection.publish(topic, new AsciiBuffer("SHUTDOWN"), QoS.EXACTLY_ONCE, true));
//        while (!queue.isEmpty()) {
//            queue.removeFirst().await();
//        }

        connection.disconnect().await();
        System.out.println("used :" + (System.currentTimeMillis() - start));
        Thread.sleep(600000);
//        System.exit(0);
    }

}
