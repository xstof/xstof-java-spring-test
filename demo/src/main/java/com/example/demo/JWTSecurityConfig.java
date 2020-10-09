package com.example.demo;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;

//@Configuration
@EnableWebSecurity
public class JWTSecurityConfig extends WebSecurityConfigurerAdapter {
 
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          //.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
          //.and()
          .authorizeRequests(authz -> authz
            .antMatchers(HttpMethod.GET, "/greeting").permitAll()
            .antMatchers(HttpMethod.GET, "/foo").hasAnyAuthority("SCOPE_write")
            .antMatchers(HttpMethod.POST, "/foo").hasRole("staff")
            //.antMatchers(HttpMethod.GET, "/foo/**").hasAuthority("SCOPE_read")
            //.antMatchers(HttpMethod.POST, "/foo").hasAuthority("SCOPE_write")
            .anyRequest().anonymous())
          .oauth2ResourceServer(oauth2 -> oauth2.jwt());
	}
}

// TODO: a session cookie is still being set when accessing /foo - why ?
// TODO: test by setting a jwt in the header