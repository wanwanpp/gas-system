package com.wp.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 王萍 on 2017/5/12 0012.
 */
public class WriteThread implements Runnable {

    public static List testString = new ArrayList(10001);

    public static InfluxDB influxDB = InfluxDBFactory.connect("http://172.23.253.30:8086", "root", "root");

    private static String dbName="batchInsert";

    static {
        for (int i = 1; i <= 5000; i++) {
            testString.add("test1,id=" + i + " pressure=1 ");
        }
        influxDB.createDatabase(dbName);
    }

    private String name = "";

    public WriteThread(String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void run() {
        influxDB.write(dbName, "autogen", InfluxDB.ConsistencyLevel.ONE, testString);
        System.out.println(name+" saved.");
    }
}
