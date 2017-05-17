package com.wp.influxdb;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimerTest extends TimerTask {

    private String jobName = "";

    public TimerTest(String jobName) {
        super();
        this.jobName = jobName;
    }

    @Override
    public void run() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        System.out.println("execute " + jobName);
        System.out.println(dateFormat.format(new Date()));
    }

    public static void main(String[] args) {
        Timer timer = new Timer();
//        long delay1 = 1 * 1000;
//        long period1 = 1000;
//// 从现在开始 1 秒钟之后，每隔 1 秒钟执行一次 job1
//        timer.schedule(new TimerTest("job1"), delay1, period1);
        long delay2 =1000;
        long period2 = 3000;
        timer.schedule(new TimerTest("job2"), delay2, period2);
    }
} 