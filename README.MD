# AAD Authentication with Java Spring

## Stateless token auth towards Java Spring REST service

### Using Spring Security natively

- Use [Spring Security ResourceServer](https://docs.spring.io/spring-security/site/docs/5.4.1/reference/html5/#oauth2resourceserver) to act upon **scopes** as authorities by default
- by default this will **not** take into account any AAD roles
- configure, [as documented](https://docs.spring.io/spring-security/site/docs/5.4.1/reference/html5/#specifying-the-authorization-server), below sample with in `application.properties` the issuer-uri as prefix for the oidc metadata endpoint
- configure app registration for REST api to accept AAD v2 tokens in the manifest
- need to validate token audience explicitly, [as documented](https://docs.spring.io/spring-security/site/docs/5.4.1/reference/html5/#oauth2resourceserver-jwt-validation-custom)

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

import java.util.List;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;

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
          .anyRequest().anonymous())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt())
        );
  }

  // make sure to validate the audience of the token:
  OAuth2TokenValidator<Jwt> getAudienceValidator() {
    String AUD_CLAIM_NAME = "aud";
    String validAudienceForThisRESTApi = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
    return new JwtClaimValidator<List<String>>(AUD_CLAIM_NAME, aud -> aud.contains(validAudienceForThisRESTApi));
  }

  // auto-wired bean for decoding and validating jwt tokens, configured for validating both issuer and audience
  @Bean
  JwtDecoder jwtDecoder() {
    String issuerUri = "https://login.microsoftonline.com/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx/v2.0";
    NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
        JwtDecoders.fromIssuerLocation(issuerUri);

    OAuth2TokenValidator<Jwt> audienceValidator = getAudienceValidator();
    OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
    OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

    jwtDecoder.setJwtValidator(withAudience);

    return jwtDecoder;
  }
}
~~~

### Working with roles

By default the Spring Security ResouceServer is not aware of AAD role claims and only deals with scopes.  To customize this behavior in Spring, we can leverage [`JwtAuthenticationConverter`](https://docs.spring.io/spring-security/site/docs/5.4.1/reference/html5/#oauth2resourceserver-jwt-authorization-extraction) to convert the Jwt into a collection of granted authorities.


To replace scopes with roles as authorities:

~~~java
@EnableWebSecurity
public class JWTSecurityConfig extends WebSecurityConfigurerAdapter {
 
  @Override
  protected void configure(HttpSecurity http) throws Exception {
      http
        .authorizeRequests(authz -> authz
          .antMatchers(HttpMethod.GET, "/greeting").permitAll()
          .antMatchers(HttpMethod.POST, "/foo").hasRole("staff")
          .anyRequest().anonymous())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt()
                                              .jwtAuthenticationConverter(getAADRoleAwareJwtAuthenticationConverter())
        );
  }

  private JwtAuthenticationConverter getAADRoleAwareJwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
    grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return jwtAuthenticationConverter;
  }

  // make sure to validate the audience of the token:
  OAuth2TokenValidator<Jwt> getAudienceValidator() { /*...*/ }

  // auto-wired bean for decoding and validating jwt tokens, configured for validating both issuer and audience
  @Bean
  JwtDecoder jwtDecoder() { /*...*/ }
}
~~~

### Using a simple test controller to see what scopes or roles a token contains:

The below sample code leverages the [`Authentication`](https://docs.spring.io/spring-security/site/docs/5.4.1/reference/html5/#servlet-authentication-authentication)

~~~java
@RestController
@RequestMapping(value = "/tokeninfo")
public class MeController {

    @GetMapping()
    public Object getInfoAboutToken(@CurrentSecurityContext(expression = "authentication") Authentication authentication){
        return authentication.getAuthorities().toArray(); // <= returns list of authorities (scopes or roles, depending on config)
    }
}
~~~