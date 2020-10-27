package com.example.demospringmvc;

import java.util.*;

import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class OAuth2LoginSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(authorize -> authorize
                .anyRequest().authenticated()
            )
            .oauth2Login(Customizer.withDefaults());
            //.oauth2Login( oauth2 -> oauth2.)
            // .oauth2Login(oauth2 -> 
            //     oauth2.userInfoEndpoint(userInfo -> 
            //         userInfo.userAuthoritiesMapper(userAuthoritiesMapper())
            //     )
            // );
    }

    // work with AAD application roles:
    // see: https://docs.spring.io/spring-security/site/docs/5.4.1/reference/html5/#oauth2login-advanced
    @Bean
    GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (OidcUserAuthority.class.isInstance(authority)) {
                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority)authority;

                    OidcIdToken idToken = oidcUserAuthority.getIdToken();
                    // OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

                    // Map the claims found in idToken and/or userInfo
                    // to one or more GrantedAuthority's and add it to mappedAuthorities
                    if(idToken.containsClaim("roles")){
                        for (String role : idToken.getClaimAsStringList("roles")) {
                            mappedAuthorities.add(authority);
                            mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                        }
                    }

                } else if (SimpleGrantedAuthority.class.isInstance(authority)){
                    mappedAuthorities.add(authority);
                } else if (OAuth2UserAuthority.class.isInstance(authority)) {
                    // OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority)authority;

                    // Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();

                    // // Object attributes = oauth2UserAuthority.getAttributes();
                    // String a = "test";

                    // Map the attributes found in userAttributes
                    // to one or more GrantedAuthority's and add it to mappedAuthorities

                }
            });

            return mappedAuthorities;
        };
    }
}