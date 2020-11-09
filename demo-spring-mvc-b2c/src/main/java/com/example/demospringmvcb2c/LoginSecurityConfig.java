package main.java.com.example.demospringmvcb2c;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class LoginSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(authorize -> authorize
                .anyRequest().authenticated()
            )
            .oauth2Login(Customizer.withDefaults());
    }

    @Bean
    GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Logger logger = LoggerFactory.getLogger(GrantedAuthoritiesMapper.class);

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
                }
            });

            logger.info("The customized user authorities: {}", mappedAuthorities);
            return mappedAuthorities;
        };
    }

    // @Bean
    // JwtDecoder jwtDecoder() {
    //     String issuerUri = "https://login.microsoftonline.com/3fd11e85-d8ce-4c7f-b6a0-816346615777/v2.0"
    //     NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuerUri);

    //     OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator("");
    //     OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer();
    //     OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

    //     jwtDecoder.setJwtValidator(withAudience);

    //     return jwtDecoder;
    // }
}