package com.wp.protobuf;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by 王萍 on 2017/5/22 0022.
 */
public class GasMsgTest {

    @Test
    public void testEnCode() throws IOException {

        //构造数据容器
        GasMsg.GasDataBox.Builder boxBuilder = GasMsg.GasDataBox.newBuilder();
        GasMsg.GasData.Builder gasBuilder = null;
        for (int i = 0; i < 100000; i++) {
            gasBuilder = GasMsg.GasData.newBuilder();
            gasBuilder.setId(i);
            gasBuilder.setPressure(i);
            gasBuilder.setTemper(i);
            gasBuilder.setSFlow(i);
            gasBuilder.setWFlow(i);
            gasBuilder.setAFlow(i);
            gasBuilder.setTime(System.currentTimeMillis());
            boxBuilder.addGasData(gasBuilder);
        }

        GasMsg.GasDataBox gasDataBox = boxBuilder.build();
//        System.out.println(gasDataBox);

        //序列化
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        gasDataBox.writeTo(bos);
        byte[] bytes = bos.toByteArray();

        System.out.println(bytes.length);

        //反序列化
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        GasMsg.GasDataBox gasDataBox2 = GasMsg.GasDataBox.parseFrom(bis);
//        System.out.println(gasDataBox2);


        Iterator<GasMsg.GasData> iterator = gasDataBox2.getGasDataList().iterator();
//        while (iterator.hasNext()){
//            System.out.println(iterator.next().getId());
//        }


    }
}
