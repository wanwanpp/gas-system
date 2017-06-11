package com.wp.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import java.util.List;

/**
 * Created by 王萍 on 2017/5/24 0024.
 */
public class InfluxTemplate {

    public InfluxDB influxDB = null;

    private String db = "gasData";

//    {
//        influxDB.createDatabase(db);
//    }

    public InfluxTemplate() {
        influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
        influxDB.createDatabase(db);
    }

    public void write(List<String> list) {
        influxDB.write(db, "autogen", InfluxDB.ConsistencyLevel.ONE, list);
    }


}
