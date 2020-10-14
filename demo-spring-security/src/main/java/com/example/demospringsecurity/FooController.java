package com.example.demospringsecurity;

import java.util.*;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping(value = "/foo")
public class FooController {
 
    // Logger logger = LoggerFactory.getLogger(FooController.class);

    @GetMapping(value = "/{id}")
    public Foo findOne(@PathVariable Long id) {
        return new Foo(randomNumeric(), "one");
    }
 
    @GetMapping
    public List findAll() {
        List fooList = new ArrayList();
        fooList.add(new Foo(randomNumeric(), "one"));
        fooList.add(new Foo(randomNumeric(), "two"));
        fooList.add(new Foo(randomNumeric(), "three"));
        return fooList;
    }
 
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void create(@RequestBody Foo newFoo) {
        //logger.info("Foo created");
    }

    private double randomNumeric(){
        double x = Math.random();
        return x;
    }
}