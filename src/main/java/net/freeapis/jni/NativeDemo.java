package net.freeapis.jni;

/**
 * freeapis, Inc.
 * Copyright (C): 2016
 * <p>
 * Description:
 * TODO
 * <p>
 * Author:Administrator
 * Date:2017年12月11日 16:15
 */
public class NativeDemo {

    //本地输出控制台
    public static native void print();

    //计算加法
    public native int sum(int x,int y);
}
