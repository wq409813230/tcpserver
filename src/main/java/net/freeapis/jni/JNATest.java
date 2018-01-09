package net.freeapis.jni;

import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * freeapis, Inc.
 * Copyright (C): 2016
 * <p>
 * Description:
 * TODO
 * <p>
 * Author:Administrator
 * Date:2017年12月11日 17:55
 */
public class JNATest {

    public static void main(String[] args) {

        INative Instance = (INative) Native.loadLibrary((Platform.isWindows() ? "jnaDemo" : "c"), INative.class);
        System.out.println(Instance.add(2,3));
        Instance.console("你好");
    }
}
