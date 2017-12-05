package com.company;

/**
 * @Author:LUJIPENG
 * @Description:
 * @Date:Created in 14:57 2017/12/4
 * @Modified By:
 */
public class LuException extends RuntimeException {
    String exceptionMessage;
    public LuException(String s){
        exceptionMessage = s;
    }
    public void showExceptionToUser(){
        System.err.println(exceptionMessage);
    }
}
