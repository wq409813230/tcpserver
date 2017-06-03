package com.lexing.protocol;

/**
 * freeapis, Inc.
 * Copyright (C): 2016
 * <p>
 * Description:西电设备协议报文段枚举
 * TODO
 * <p>
 * Author:Administrator
 * Date:2017年06月03日 13:22
 */
public enum ProtocolFragment {

    /**
     * 设备SN
     */
    Sn(8,null),
    /**
     * 数据长度
     */
    DataLength(2,Sn),
    /**
     * 命令字
     */
    Command(2,DataLength),
    /**
     * 设备当前时间
     */
    RTC(6,Command),
    /**
     * 非电量信息
     */
    NonElectric(4,RTC),
    /**
     * 常规连续量信息
     */
    NormalInfo(24,NonElectric),
    /**
     * 特殊连续量
     */
    SpecialInfo(24,NormalInfo),
    /**
     * 设备故障状态
     */
    ErrorStatus(2,SpecialInfo),
    /**
     * 高级连续量
     */
    AdvancedInfo(72,ErrorStatus),
    /**
     * 无线测温
     */
    WirelessoC(36,AdvancedInfo);

    private int length;
    private ProtocolFragment prevFragment;

    private ProtocolFragment(int length,ProtocolFragment prevFragment){
        this.length = length;
        this.prevFragment = prevFragment;
    }

    /**
     * 根据命令字获取下一个协议段
     * @param command
     * @return
     */
    public ProtocolFragment nextFragment(int command){
        ProtocolFragment currentFragment = this;
        ProtocolFragment nextFragment = null;
        boolean isExpected = false;
        for(;;){
            nextFragment = currentFragment.nextFragment();
            isExpected = (nextFragment != AdvancedInfo && nextFragment != WirelessoC)
                    || (nextFragment == AdvancedInfo && (command == 1 || command == 3))
                    || (nextFragment == WirelessoC && (command == 0 || command == 3));
            currentFragment = nextFragment;
            if(isExpected) break;
        }
        return nextFragment;
    }

    private ProtocolFragment nextFragment(){
        ProtocolFragment nextFragment = null;
        for(ProtocolFragment protocolFragment : ProtocolFragment.values()){
            if(protocolFragment.prevFragment == this){
                nextFragment = protocolFragment;
                break;
            }
        }
        return nextFragment;
    }

    public static void main(String[] args) {
        int command = 1;
        ProtocolFragment currentFragment = ProtocolFragment.Sn;
        ProtocolFragment nextFragment = null;
        for(;;){
            nextFragment = currentFragment.nextFragment(command);
            if(nextFragment == null) {
                System.out.println(currentFragment);
                break;
            }
            System.out.print(currentFragment + "->");
            currentFragment = nextFragment;
        }
    }
}
