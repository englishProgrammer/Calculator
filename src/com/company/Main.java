package com.company;

public class Main {

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.input();
        long start = System.nanoTime();
        System.out.println(calculator.getResult());
        long end = System.nanoTime();
        long runTime = end - start;
        System.out.println("runTime:\t" + runTime);
        // write your code here
    }
}
