package net.freeapis.jni;

import com.sun.jna.Library;

/**
 * freeapis, Inc.
 * Copyright (C): 2016
 * <p>
 * Description:
 * TODO
 * <p>
 * Author:Administrator
 * Date:2017年12月11日 17:51
 */
public interface INative extends Library {

    //计算加法
    int add(int a, int b);

    void console(String message);
}
