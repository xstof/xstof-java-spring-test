package fta.identity.sample.aad3auth;

import com.azure.spring.aad.webapp.AADWebSecurityConfigurerAdapter;

import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

//@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class Aad3AuthSecurityConfig  extends AADWebSecurityConfigurerAdapter {
    /**
    * Add configuration logic as needed.
    */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
        .authorizeRequests()
        .antMatchers("/").permitAll()
        // .anyRequest().fullyAuthenticated() // => cannot do this anymore at this point
        ;

        super.configure(http); // => this causes redirect to AAD to happen for pages other than root: '/'
    }
}
