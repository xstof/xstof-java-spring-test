package com.example.demoazuresdk;

public class Foo {
    private double id;
    private String name;
    
    // constructor, getters and setters
    public Foo(double id, String name){
        this.id = id;
        this.name = name;
    }

    public double getId(){
        return id;
    }

    public String getName(){
        return name;
    }
    
    public static Foo NewRandomFoo(){
        return new Foo(randomNumeric(), "foo");
    }

    public static Foo NewRandomFoo(String name){
        return new Foo(randomNumeric(), name);
    }

    private static double randomNumeric(){
        double x = Math.random();
        return x;
    }
}