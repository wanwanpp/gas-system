package com.wp.api;

/**
 * Created by 王萍 on 2017/6/8 0008.
 */
public interface MqttConnectionCycle {

    void config(String user, String password, String host, int port);

    void connect();

    void listener();

    void disconnect();
}
