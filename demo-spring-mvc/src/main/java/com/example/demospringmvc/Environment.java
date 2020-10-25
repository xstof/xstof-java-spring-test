package com.example.demospringmvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;

public class Environment {
    @Autowired
    private ConfigurableEnvironment env;

    public Environment(){
        env.setActiveProfiles("dev");
    }
}
