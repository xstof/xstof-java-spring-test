package com.example.demoazuresdk;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/foo")
public class FooController {

    @GetMapping
    public List all(){
        List fooList = new ArrayList();
        fooList.add(Foo.NewRandomFoo("first"));
        fooList.add(Foo.NewRandomFoo("second"));
        fooList.add(Foo.NewRandomFoo("third"));

        return fooList;
    }
}
