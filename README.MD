# AAD Authentication with Java Spring

## Stateless token auth towards Java Spring REST service

### Using Spring Security natively

- Use Spring Security ResourceServer to act upon **scopes** as authorities by default
- by default this will not take into account any AAD roles
- configure below sample with in `application.properties` the issuer-uri as prefix for the oidc metadata endpoint
- configure app registration for REST api to accept AAD v2 tokens in the manifest

~~~text
// application.properties:
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://login.microsoftonline.com/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx/v2.0
~~~

~~~json
{
    // REST API app registration manifest
	"id": "d8948acc-596d-4505-817f-60d71eae9e4a",
	"acceptMappedClaims": null,
	"accessTokenAcceptedVersion": 2, // <= make sure v2 tokens are delivered
	"..."
}
~~~

~~~java
package com.example.demo;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.oauth2.server.resource.authentication.*;

@EnableWebSecurity
public class JWTSecurityConfig extends WebSecurityConfigurerAdapter {
 
  @Override
  protected void configure(HttpSecurity http) throws Exception {
      http
        .authorizeRequests(authz -> authz
          .antMatchers(HttpMethod.GET, "/greeting").permitAll()
          .antMatchers(HttpMethod.GET, "/me").permitAll()
          .antMatchers(HttpMethod.GET, "/foo/**").hasAuthority("SCOPE_read")
          .antMatchers(HttpMethod.POST, "/foo").hasAuthority("SCOPE_write")
          .anyRequest().anonymous())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt()));
  }
}
~~~

To replace scopes with roles as authorities:

~~~java
@Configuration
@EnableWebSecurity
public class JWTSecurityConfig extends WebSecurityConfigurerAdapter {
 
  @Override
  protected void configure(HttpSecurity http) throws Exception {
      http
        .authorizeRequests(authz -> authz
          .antMatchers(HttpMethod.POST, "/foo").hasRole("staff")
          .anyRequest().anonymous())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt()
                                              .jwtAuthenticationConverter(
                                                AADJwtAuthenticationConverter()
                                              ));
  }

  @Bean
  public JwtAuthenticationConverter AADJwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthoritiesClaimName("roles"); // => replaces scopes with roles

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return jwtAuthenticationConverter;
  }
}
~~~