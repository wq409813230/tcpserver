package net.freeapis.reactor;

/**
 * Created by wuqiang on 2017/6/11.
 */
public class NIOServer {

    public static void main(String[] args) throws Exception{
        new Reactor(Reactor.ServerType.tcp).start();
    }
}
