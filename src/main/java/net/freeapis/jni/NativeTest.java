package net.freeapis.jni;

/**
 * freeapis, Inc.
 * Copyright (C): 2016
 * <p>
 * Description:
 * TODO
 * <p>
 * Author:Administrator
 * Date:2017年12月11日 17:15
 */
public class NativeTest {

    static {
        System.loadLibrary("native");
    }

    public static void main(String[] args) {
        NativeDemo aNative = new NativeDemo();
        NativeDemo.print();
        int sum = aNative.sum(2,3);
        System.out.println(sum);
    }
}
