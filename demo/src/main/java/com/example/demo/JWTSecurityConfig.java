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
          .antMatchers(HttpMethod.GET, "/me").permitAll()
          .antMatchers(HttpMethod.GET, "/foo").hasAnyAuthority("SCOPE_write")
          .antMatchers(HttpMethod.POST, "/foo").hasRole("staff")
          //.antMatchers(HttpMethod.GET, "/foo/**").hasAuthority("SCOPE_read")
          //.antMatchers(HttpMethod.POST, "/foo").hasAuthority("SCOPE_write")
          .anyRequest().anonymous())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt()
                                              .jwtAuthenticationConverter(getAADRoleAwareJwtAuthenticationConverter())
        );
  }

  OAuth2TokenValidator<Jwt> getAudienceValidator() {
    String AUD_CLAIM_NAME = "aud";
    String validAudienceForThisRESTApi = "ae0a5b6e-5190-4a52-bc30-6f8645a5f262";
    return new JwtClaimValidator<List<String>>(AUD_CLAIM_NAME, aud -> aud.contains(validAudienceForThisRESTApi));
  }

  @Bean
  JwtDecoder jwtDecoder() {
    String issuerUri = "https://login.microsoftonline.com/3fd11e85-d8ce-4c7f-b6a0-816346615777/v2.0";
    NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
        JwtDecoders.fromIssuerLocation(issuerUri);

    OAuth2TokenValidator<Jwt> audienceValidator = getAudienceValidator();
    OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
    OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

    jwtDecoder.setJwtValidator(withAudience);

    return jwtDecoder;
  }

  // @Bean <= autowiring should be possible as of Spring 5.4
  // see: https://github.com/spring-projects/spring-security/issues/8185
  private JwtAuthenticationConverter getAADRoleAwareJwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
    grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return jwtAuthenticationConverter;
  }
}


// TODO: a session cookie is still being set when accessing /foo - why ?
// TODO: test by setting a jwt in the header