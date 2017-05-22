package com.wp.protobuf.samples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by 王萍 on 2017/5/21 0021.
 */
public class Encoder {
    public static void main(String[] args) throws IOException {
        PersonMsg.Person.Builder personBuilder = PersonMsg.Person.newBuilder();
        personBuilder.setId(1);
        personBuilder.setName("wanwanpp");
        personBuilder.setName("wanwanpp@163.com");
        personBuilder.addAllFriends((Iterable<String>) Arrays.asList("Friend A", "Friend B"));

        PersonMsg.Person wp = personBuilder.build();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wp.writeTo(bos);

        byte[] byteArray = bos.toByteArray();
        for (byte b : byteArray) {
            System.out.print((char) b);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        PersonMsg.Person person = PersonMsg.Person.parseFrom(bis);
        System.out.println(person);
    }
}
