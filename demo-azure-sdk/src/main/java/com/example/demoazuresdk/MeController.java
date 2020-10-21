package com.example.demoazuresdk;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/me")
public class MeController {

    @GetMapping()
    public Object getInfoAboutMe(@CurrentSecurityContext(expression = "authentication") Authentication authentication){
        return authentication.getAuthorities().toArray();
    }
}