package com.wp.protobuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by 王萍 on 2017/5/22 0022.
 */
public class GasDataUtil {

    /**
     * 产生随机数据模拟
     *
     * @return
     * @throws IOException
     */
    public byte[] produceGasData() throws IOException {

        //随机数
        Random random = new Random();

        //构造数据容器
        GasMsg.GasDataBox.Builder boxBuilder = GasMsg.GasDataBox.newBuilder();
        GasMsg.GasData.Builder gasBuilder = null;
//        for (int i = 0; i < 20000; i++) {
        for (int i = 0; i < 1; i++) {
            gasBuilder = GasMsg.GasData.newBuilder();
            gasBuilder.setId(i);
            gasBuilder.setPressure(random.nextFloat() * 100);
            gasBuilder.setTemper(random.nextFloat() * 100);
            gasBuilder.setSFlow(random.nextFloat() * 100);
            gasBuilder.setWFlow(random.nextFloat() * 100);
            gasBuilder.setAFlow(random.nextFloat() * 100);
            gasBuilder.setTime(System.currentTimeMillis());
            boxBuilder.addGasData(gasBuilder);
        }

        GasMsg.GasDataBox gasDataBox = boxBuilder.build();

        //序列化
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        gasDataBox.writeTo(bos);
        byte[] bytes = bos.toByteArray();

        return bytes;
    }

    /**
     * 反序列化
     *
     * @param bytes
     * @return
     */
    public GasMsg.GasDataBox consume(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            return GasMsg.GasDataBox.parseFrom(bis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
