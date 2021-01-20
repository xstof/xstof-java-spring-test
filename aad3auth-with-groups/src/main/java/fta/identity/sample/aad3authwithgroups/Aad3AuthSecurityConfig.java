package fta.identity.sample.aad3authwithgroups;

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
        // for default implementation see: https://github.com/Azure/azure-sdk-for-java/blob/c92f8071a051b9b6212529ac86955c5e972b9f16/sdk/spring/azure-spring-boot/src/main/java/com/azure/spring/aad/webapp/AADWebSecurityConfigurerAdapter.java#L42
    }
}
