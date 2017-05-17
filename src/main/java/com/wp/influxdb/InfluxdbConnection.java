package com.wp.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by 王萍 on 2017/5/11 0011.
 */
public class InfluxdbConnection {

    InfluxDB influxDB;
    List testString = new ArrayList(10001);

    @Before
    public void setUp() throws InterruptedException {
//        influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
        influxDB = InfluxDBFactory.connect("http://172.23.253.30:8086", "root", "root");
//        String dbName = "batchInsert";
//
//        for (int i = 1; i <= 100000; i++) {
////            testString.add("cpu,atag=test1 idle=100,usertime=10,system=1");
////            testString.add("test1,id="+i+" pressure=1 ");
//            this.influxDB.write(dbName,"autogen",InfluxDB.ConsistencyLevel.ONE,"test1,id="+i+" pressure=1");
//        }
//        System.out.println();
    }

    @Test
    public void testBatchInsert() {
        String dbName = "batchInsert";
        this.influxDB.createDatabase(dbName);

        this.influxDB.write(dbName, "autogen", InfluxDB.ConsistencyLevel.ONE, testString);
    }

    @Test
    public void testBatchInsert1() {
        String dbName = "batchInsert";

        for (int i = 1; i <= 100000; i++) {
            this.influxDB.write(dbName, "autogen", InfluxDB.ConsistencyLevel.ONE, "test1,id=" + i + " pressure=1");
        }
    }

    @Test
    public void testBatchInsert2() {
        final String dbName = "batchInsert";
        int i;
        this.influxDB.createDatabase(dbName);
        for (i = 1; i <= 5000; i++) {
            testString.add("test1,id=" + i + " pressure=1 ");
        }

        long start = System.currentTimeMillis();
        while (true) {

            for (int j = 0; j < 100; j++) {
                new Runnable() {
                    public void run() {
                        influxDB.write(dbName, "autogen", InfluxDB.ConsistencyLevel.ONE, testString);
                    }
                }.run();
            }
        }
//        System.out.println("use:   " + (System.currentTimeMillis() - start) + "ms");
    }

    @Test
    public void testBatchInsert3() {

        ScheduledExecutorService service = Executors.newScheduledThreadPool(20);
        for (int i = 0; i < 20; i++) {
            service.scheduleAtFixedRate(new WriteThread("thread" + i), 0, 1, TimeUnit.SECONDS);
        }
    }

    public static void main(String[] args) {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(20);
        for (int i = 0; i < 20; i++) {
            service.scheduleAtFixedRate(new WriteThread("thread" + i), 0, 500,TimeUnit.MILLISECONDS);
        }
    }

//    public static void main(String[] args) {
//        final List testString = new ArrayList(10001);
//        final InfluxDB influxDB = InfluxDBFactory.connect("http://172.23.253.30:8086", "root", "root");
//
//        Timer timer = new Timer();
//
//        final String dbName = "batchInsert";
//        int i;
//        influxDB.createDatabase(dbName);
//        for (i = 1; i <= 5000; i++) {
//            testString.add("test1,id=" + i + " pressure=1 ");
//        }
//
//        long start = System.currentTimeMillis();
////        for (int j = 0; j < 200; j++) {
//////            TimerTask task = new TimerTask() {
//////                @Override
//////                public void run() {
//////                    influxDB.write(dbName, "autogen", InfluxDB.ConsistencyLevel.ONE, testString);
//////                }
//////            };
////            timer.scheduleAtFixedRate(new TimerTask() {
////                                          @Override
////                                          public void run() {
////                                              influxDB.write(dbName, "autogen", InfluxDB.ConsistencyLevel.ONE, testString);
////                                              System.out.println(Thread.currentThread().getName()+" saved");
////                                          }
////                                      }, 0,
////                    100);
////        }
//
//        timer.scheduleAtFixedRate(new TimerTask() {
//                                      @Override
//                                      public void run() {
//                                          influxDB.write(dbName, "autogen", InfluxDB.ConsistencyLevel.ONE, testString);
//                                          System.out.println(Thread.currentThread().getName() + " saved");
//                                      }
//                                  }, 0,
//                100);
//        System.out.println("use:   " + (System.currentTimeMillis() - start) + "ms");
//    }

    @Test
    public void testSelect() {
        String dbName = "writeunittest_" + System.currentTimeMillis();
        this.influxDB.createDatabase(dbName);

        this.influxDB.write(dbName, "autogen", InfluxDB.ConsistencyLevel.ONE, Arrays.asList(
                "cpu,atag=test1 idle=100,usertime=10,system=1",
//                "cpu,atag=test1 idle=400,usertime=40,system=4",
                "cpu,atag=test2 idle=200,usertime=20,system=2",
                "cpu,atag=test3 idle=300,usertime=30,system=3"
        ));
        Query query = new Query("SELECT * FROM cpu GROUP BY *", dbName);
        QueryResult result = this.influxDB.query(query);
//        System.out.println(result.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString());
        System.out.println(result.getResults().get(0).getSeries());

//        Assert.assertEquals(result.getResults().get(0).getSeries().size(), 3);
//        Assert.assertEquals(result.getResults().get(0).getSeries().get(0).getTags().get("atag"), "test1");
//        Assert.assertEquals(result.getResults().get(0).getSeries().get(1).getTags().get("atag"), "test2");
//        Assert.assertEquals(result.getResults().get(0).getSeries().get(2).getTags().get("atag"), "test3");
//        this.influxDB.deleteDatabase(dbName);
    }

    @Test
    public void connection() {

        String dbName = "aTimeSeries";
        influxDB.createDatabase(dbName);

        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .tag("async", "true")
                .retentionPolicy("autogen")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        Point point1 = Point.measurement("cpu")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("idle", 90L)
                .addField("user", 9L)
                .addField("system", 1L)
                .build();
        Point point2 = Point.measurement("disk")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("used", 80L)
                .addField("free", 1L)
                .build();
        batchPoints.point(point1);
        batchPoints.point(point2);
        influxDB.write(batchPoints);
        Query query = new Query("SELECT idle FROM cpu", dbName);
        influxDB.query(query);
        influxDB.deleteDatabase(dbName);
    }

    @Test
    public void testWrite() {
        String dbName = "aTimeSeries";
        influxDB.createDatabase(dbName);

// Flush every 2000 Points, at least every 100ms
        influxDB.enableBatch(2000, 100, TimeUnit.MILLISECONDS);

        Point point1 = Point.measurement("cpu")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("idle", 90L)
                .addField("user", 9L)
                .addField("system", 1L)
                .build();
        Point point2 = Point.measurement("disk")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("used", 80L)
                .addField("free", 1L)
                .build();

        influxDB.write(dbName, "autogen", point1);
        influxDB.write(dbName, "autogen", point2);
        Query query = new Query("SELECT idle FROM cpu", dbName);
        influxDB.query(query);
        influxDB.deleteDatabase(dbName);
    }
}
