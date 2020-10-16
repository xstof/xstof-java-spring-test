package com.example.demomvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {
   @Autowired
   //@PreAuthorize("hasRole('Users')")
   @RequestMapping("/")
   public String helloWorld() {
      return "Hello World!";
   }
}